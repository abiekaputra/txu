import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './styles/base.css';
import './styles/components.css';
import './styles/landing.css';
import './styles/product.css';
import './styles/responsive.css';
import { App } from './app/App.tsx';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
