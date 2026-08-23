/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', '-apple-system', 'Segoe UI', 'sans-serif'],
      },
      colors: {
        brand: {
          50: '#f2f4ff',
          100: '#e6e9fe',
          200: '#cdd2fd',
          300: '#a8b0fa',
          400: '#7c82f3',
          500: '#5c5ce8',
          600: '#4740d4',
          700: '#3a33ae',
          800: '#312c8c',
          900: '#2b2971',
          950: '#191748',
        },
      },
      boxShadow: {
        soft: '0 1px 2px rgba(15, 23, 42, 0.04), 0 8px 24px -12px rgba(15, 23, 42, 0.12)',
        softer: '0 1px 2px rgba(15, 23, 42, 0.03), 0 4px 12px -6px rgba(15, 23, 42, 0.08)',
      },
    },
  },
  plugins: [],
}
