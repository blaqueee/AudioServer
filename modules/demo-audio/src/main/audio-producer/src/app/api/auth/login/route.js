import axios from 'axios';
import { NextResponse } from 'next/server';
import {AXELOR_HTTP_BASE_URL} from '@/config.js'


export async function POST(request) {
  const { username, password } = await request.json();

  if (!username || !password) {
    return NextResponse.json({ message: 'Имя пользователя и пароль обязательны' }, { status: 400 });
  }

  try {
    const authHeader = 'Basic ' + Buffer.from(`<span class="math-inline">\{username\}\:</span>{password}`).toString('base64');

    const axelorResponse = await axios.post(`${AXELOR_HTTP_BASE_URL}/callback`, {}, {
      headers: {
        'Authorization': authHeader,
      },
      withCredentials: true,
      validateStatus: (status) => status >= 200 && status < 300
    });

    const setCookieHeader = axelorResponse.headers['set-cookie'];
    const xCsrfTokenHeader = axelorResponse.headers['x-csrf-token'];

    let jsessionId = null;
    if (setCookieHeader) {
      for (const cookieString of setCookieHeader) {
        const match = cookieString.match(/JSESSIONID=([^;]+)/);
        if (match) {
          jsessionId = match[1];
          break;
        }
      }
    }

    const csrfToken = xCsrfTokenHeader;

    if (!jsessionId || !csrfToken) {
      console.error('Axelor не вернул ожидаемые заголовки JSESSIONID или X-CSRF-TOKEN.');
      return NextResponse.json({ message: 'Ошибка аутентификации: отсутствуют токены от бэкенда.' }, { status: 500 });
    }

    return NextResponse.json({
      message: 'Аутентификация успешна',
      jsessionId: jsessionId,
      csrfToken: csrfToken
    });

  } catch (error) {
    console.error('Ошибка при аутентификации с Axelor:', error.response ? error.response.data : error.message);
    return NextResponse.json(
      { message: 'Аутентификация не удалась', error: error.message },
      { status: error.response?.status || 500 }
    );
  }
}