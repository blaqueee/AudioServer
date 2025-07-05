// src/app/page.js
import AudioStreamer from '../components/AudioStreamer';

export default function HomePage() {
  return (
    <main style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', backgroundColor: '#e0f2f7' }}>
      <AudioStreamer />
    </main>
  );
}
