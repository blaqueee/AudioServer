/** @type {import('next').NextConfig} */

import {CUSTOMS_OFFICE_BASE_URL} from './src/config.js';

const nextConfig = {
    // Only use export settings in production
    ...(process.env.NODE_ENV === 'production' && {
        output: 'export',
        distDir: 'build',
        basePath: '/audio/audio-producer/out',
        assetPrefix: '/audio/audio-producer/out/',
    }),
    
    async rewrites() {
      return [
        {
          source: '/api/axelor-data/:path*',
          destination: `${CUSTOMS_OFFICE_BASE_URL}/:path*`,
        },
      ];
    },
  };
  
  export default nextConfig;
  