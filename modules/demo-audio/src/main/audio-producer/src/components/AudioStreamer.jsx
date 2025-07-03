"use client";

import React, { useRef, useState, useEffect } from 'react';
import { Button, Box, Typography, CircularProgress, Alert } from '@mui/material';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import CustomsOfficeSelector from './CustomsOfficeSelector';
import {PROXY_URL} from "@/config.js";

// Create a custom theme
const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#666cff',
    },
    background: {
      default: '#31334d',
      paper: '#ffffff',
    },
  },
  typography: {
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
  },
});

const AudioStreamer = () => {
    const [isRecording, setIsRecording] = useState(false);
    const [selectedOfficeIds, setSelectedOfficeIds] = useState([]);
    const [wsStatus, setWsStatus] = useState('disconnected'); // 'disconnected', 'connecting', 'connected', 'error'
    const [microphoneAccess, setMicrophoneAccess] = useState(false); // Для индикации доступа к микрофону
    
    const mediaRecorderRef = useRef(null);
    const wsRef = useRef(null);
    const audioContextRef = useRef(null);
    const scriptProcessorRef = useRef(null);
    const mediaStreamRef = useRef(null);

    useEffect(() => {
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            alert('Ваш браузер не поддерживает MediaDevices API или getUserMedia. Пожалуйста, обновите его.');
            return;
        }
        if (!window.WebSocket) {
            alert('Ваш браузер не поддерживает WebSockets. Пожалуйста, обновите его.');
            return;
        }
        if (!window.AudioContext || !window.MediaStreamAudioSourceNode) {
            alert('Ваш браузер не поддерживает Web Audio API. Пожалуйста, обновите его.');
            return;
        }
        console.log('Браузер поддерживает необходимые API.');

        const checkMicrophone = async () => {
            try {
                const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
                stream.getTracks().forEach(track => track.stop());
                setMicrophoneAccess(true);
                console.log('Доступ к микрофону подтвержден.');
            } catch (error) {
                console.error('Ошибка доступа к микрофону:', error);
                setMicrophoneAccess(false);
                alert('Не удалось получить доступ к микрофону. Убедитесь, что он подключен и разрешите доступ в браузере.');
            }
        };
        checkMicrophone();

        return () => {
            stopRecording();
        };
    }, []);

    const startRecording = async () => {
        if (selectedOfficeIds.length === 0) {
            alert('Пожалуйста, выберите хотя бы один таможенный офис.');
            return;
        }
        if (!microphoneAccess) {
            alert('Нет доступа к микрофону. Пожалуйста, разрешите доступ и обновите страницу.');
            return;
        }

        setIsRecording(true);
        setWsStatus('connecting');

        try {
            audioContextRef.current = new (window.AudioContext || window.webkitAudioContext)();

            if (audioContextRef.current.state === 'suspended') {
                console.log('AudioContext приостановлен, попытка возобновления...');
                await audioContextRef.current.resume();
                console.log('AudioContext возобновлен.');
            }

            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            mediaStreamRef.current = stream;
            console.log('Доступ к микрофону получен.');

            const wsUrl = `${PROXY_URL}?tcpClients=${selectedOfficeIds.join(',')}`;
            wsRef.current = new WebSocket(wsUrl);
            console.log('Попытка подключения к WebSocket-прокси URL:', wsUrl);

            wsRef.current.onopen = () => {
                console.log('WebSocket к прокси подключен. Запуск обработки аудио...');
                setWsStatus('connected');

                const source = audioContextRef.current.createMediaStreamSource(stream);

                scriptProcessorRef.current = audioContextRef.current.createScriptProcessor(4096, 1, 1);
                scriptProcessorRef.current.connect(audioContextRef.current.destination);
                source.connect(scriptProcessorRef.current);

                scriptProcessorRef.current.onaudioprocess = (event) => {
                    if (wsRef.current.readyState !== WebSocket.OPEN) {
                        console.warn('onaudioprocess возвращается рано: wsState=', wsRef.current.readyState);
                        return;
                    }

                    const inputBuffer = event.inputBuffer.getChannelData(0);
                    const pcm16 = new Int16Array(inputBuffer.length);
                    for (let i = 0; i < inputBuffer.length; i++) {
                        pcm16[i] = Math.max(-1, Math.min(1, inputBuffer[i])) * 0x7FFF;
                    }

                    wsRef.current.send(pcm16.buffer);
                };
            };

            wsRef.current.onclose = (event) => {
                console.log(`WebSocket к прокси отключен. Код: ${event.code}, Причина: ${event.reason}`);
                setWsStatus('disconnected');
                stopRecording();
            };

            wsRef.current.onerror = (error) => {
                console.error('Ошибка WebSocket:', error);
                setWsStatus('error');
                alert('Произошла ошибка WebSocket. Проверьте консоль для деталей.');
                stopRecording();
            };

        } catch (error) {
            console.error('Ошибка при получении доступа к микрофону или подключении к WebSocket:', error);
            alert(`Не удалось начать запись: ${error.message}`);
            setIsRecording(false);
            setWsStatus('disconnected');
        }
    };

    const stopRecording = () => {
        setIsRecording(false);
        setWsStatus('disconnected');

        if (scriptProcessorRef.current) {
            scriptProcessorRef.current.disconnect();
            scriptProcessorRef.current.onaudioprocess = null;
            scriptProcessorRef.current = null;
        }

        if (mediaStreamRef.current) {
            mediaStreamRef.current.getTracks().forEach(track => track.stop());
            mediaStreamRef.current = null;
        }

        if (audioContextRef.current) {
            if (audioContextRef.current.state !== 'closed') {
                audioContextRef.current.close();
            }
            audioContextRef.current = null;
        }

        if (wsRef.current) {
            if (wsRef.current.readyState === WebSocket.OPEN) {
                wsRef.current.close();
            }
            wsRef.current = null;
        }
        console.log('Запись остановлена и ресурсы очищены.');
    };

    const handleOfficeSelection = (selectedIds) => {
        console.log('Выбраны таможенные офисы (ID):', selectedIds);
        setSelectedOfficeIds(selectedIds);
    };

    const getStatusColor = (status) => {
        switch (status) {
            case 'connected': return 'success.main';
            case 'connecting': return 'warning.main';
            case 'disconnected': return 'text.secondary';
            case 'error': return 'error.main';
            default: return 'text.secondary';
        }
    };

    return (
        <ThemeProvider theme={theme}>
            <CssBaseline />
            <Box 
                sx={{ 
                    p: 4, 
                    border: '1px solid #e0e0e0', 
                    borderRadius: '12px', 
                    width: '600px',
                    height: '700px',
                    overflow: 'auto',
                    position: 'fixed', 
                    top: '50%', 
                    left: '50%', 
                    transform: 'translate(-50%, -50%)', 
                    boxShadow: '0 4px 12px rgba(0,0,0,0.1)', 
                    bgcolor: 'background.paper',
                    zIndex: 1000,
                    textRendering: 'geometricPrecision',
                }}
            >
                <Typography variant="h4" component="h1" gutterBottom sx={{ textAlign: 'center', mb: 3, color: 'primary.main' }}>
                    Аудиостриминг
                </Typography>

                {/* Индикация статуса микрофона */}
                {!microphoneAccess && (
                    <Alert severity="warning" sx={{ mb: 2 }}>
                        Ожидание доступа к микрофону. Пожалуйста, разрешите доступ.
                    </Alert>
                )}

                <CustomsOfficeSelector onSelectionChange={handleOfficeSelection} />

                <Box sx={{ mt: 4, display: 'flex', flexDirection: 'column', gap: 2 }}>
                    <Button
                        variant="contained"
                        color="primary"
                        onClick={startRecording}
                        disabled={isRecording || selectedOfficeIds.length === 0 || !microphoneAccess}
                        sx={{ py: 1.5, fontSize: '1rem' }}
                    >
                        {isRecording ? (
                            <>
                                <CircularProgress size={20} color="inherit" sx={{ mr: 1 }} />
                                Передача...
                            </>
                        ) : (
                            'Начать потоковую передачу'
                        )}
                    </Button>
                    <Button
                        variant="outlined"
                        color="error"
                        onClick={stopRecording}
                        disabled={!isRecording}
                        sx={{ py: 1.5, fontSize: '1rem' }}
                    >
                        Остановить
                    </Button>
                </Box>

                {/* Индикация статуса WebSocket */}
                <Box sx={{ mt: 3, textAlign: 'center' }}>
                    <Typography variant="body2" sx={{ color: getStatusColor(wsStatus) }}>
                        Статус подключения: 
                        {wsStatus === 'connecting' && ' Подключение...'}
                        {wsStatus === 'connected' && ' Подключено'}
                        {wsStatus === 'disconnected' && ' Отключено'}
                        {wsStatus === 'error' && ' Ошибка подключения'}
                    </Typography>
                    {wsStatus === 'error' && (
                        <Typography variant="caption" color="error">
                            Проверьте консоль браузера и логи сервера.
                        </Typography>
                    )}
                </Box>
            </Box>
        </ThemeProvider>
    );
};

export default AudioStreamer;