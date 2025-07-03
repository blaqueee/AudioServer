import { WebSocket, WebSocketServer } from 'ws';
import axios from 'axios';
import {WS_PROXY_PORT,
    AXELOR_HTTP_BASE_URL,
    AXELOR_WS_BASE_URL,
    AXELOR_USER_NAME,
    AXELOR_PASSWORD} from '../src/config.js';

function parseJSessionId(setCookieHeaders) {
    if (!setCookieHeaders) return null;
    for (const cookieString of setCookieHeaders) {
        const match = cookieString.match(/JSESSIONID=([^;]+)/);
        if (match) {
            return match[1];
        }
    }
    return null;
}

const wss = new WebSocketServer({ port: WS_PROXY_PORT }, () => {
    console.log(`WebSocket Proxy Server started on port :${WS_PROXY_PORT}`);
    console.log(`Proxying to Axelor HTTP at ${AXELOR_HTTP_BASE_URL}`);
    console.log(`Proxying to Axelor WS at ${AXELOR_WS_BASE_URL}`);
});

wss.on('connection', async (clientWs, req) => {
    console.log('Client connected to proxy WebSocket.');

    let axelorWs = null;

    try {
        const authHeader = 'Basic ' + Buffer.from(`${AXELOR_USER_NAME}:${AXELOR_PASSWORD}`).toString('base64');

        console.log('Attempting to authenticate with Axelor using Basic Auth...');

        const AUTH_URL = `${AXELOR_HTTP_BASE_URL}/callback`;

        const axelorAuthResponse = await axios.post(
            AUTH_URL,
            {},
            {
                headers: {
                    'Authorization': authHeader,
                },
                withCredentials: true,
                validateStatus: (status) => status >= 200 && status < 300,
            }
        );

        const jsessionId = parseJSessionId(axelorAuthResponse.headers['set-cookie']);
        const csrfToken = axelorAuthResponse.headers['x-csrf-token'];

        if (!jsessionId || !csrfToken) {
            console.error('Failed to get JSESSIONID or CSRF-TOKEN from Axelor. Closing client connection.');
            clientWs.close(1008, 'Auth failed: tokens missing'); // Сокращено сообщение
            return;
        }

        console.log('Successfully authenticated with Axelor. Tokens obtained.');
        const clientQuery = req.url.split('?')[1] || ''; 

        let axelorWsUrl = `${AXELOR_WS_BASE_URL}/audio-stream`;
        if (clientQuery) {
            axelorWsUrl += `?${clientQuery}`; 
        }

        console.log(`Connecting to Axelor WebSocket at: ${axelorWsUrl}`); 
        axelorWs = new WebSocket(axelorWsUrl, {
            headers: {
                'Cookie': `JSESSIONID=${jsessionId};CSRF-TOKEN=${csrfToken}`,
            },
        });

        axelorWs.onopen = () => {
            console.log('Connected to Axelor WebSocket.');
            clientWs.on('message', (message) => {
                if (axelorWs && axelorWs.readyState === WebSocket.OPEN) {
                    axelorWs.send(message);
                }
            });

            axelorWs.on('message', (message) => {
                if (clientWs.readyState === WebSocket.OPEN) {
                    clientWs.send(message);
                }
            });
        };

        axelorWs.onerror = (err) => {
            console.error('Axelor WebSocket error:', err.message);
            clientWs.close(1011, 'Axelor WS error'); 
        };

        axelorWs.onclose = () => {
            console.log('Axelor WebSocket closed.');
            if (clientWs.readyState === WebSocket.OPEN) {
                clientWs.close(1000, 'Axelor WS closed'); 
            }
        };

    } catch (error) {
        console.error('Error during WebSocket proxy setup:', error.message);
        const statusCode = error.response ? error.response.status : 'N/A';
        clientWs.close(1011, `Axelor auth failed (status ${statusCode})`); 
    }

    clientWs.on('close', () => {
        console.log('Client disconnected from proxy WebSocket.');
        if (axelorWs && axelorWs.readyState === WebSocket.OPEN) {
            axelorWs.close();
        }
    });

    clientWs.on('error', (err) => {
        console.error('Client WebSocket error:', err.message);
        if (axelorWs && axelorWs.readyState === WebSocket.OPEN) {
            axelorWs.close();
        }
    });
});