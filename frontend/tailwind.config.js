/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#f5f6fb',
          100: '#e8eaf6',
          200: '#c9cdea',
          300: '#a5abd9',
          400: '#7a82c3',
          500: '#5a63ac',
          600: '#454d8f',
          700: '#383e73',
          800: '#2f345d',
          900: '#292d4d',
          950: '#191b2f',
        },
      },
    },
  },
  plugins: [],
}
