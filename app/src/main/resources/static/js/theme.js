// Theme management
class ThemeManager {
    constructor() {
        this.setupEventListeners();
    }

    setupEventListeners() {
        document.getElementById('themeToggle').addEventListener('click', () => {
            this.toggleTheme();
        });
    }

    initializeTheme() {
        const savedTheme = localStorage.getItem('theme') || CONFIG.THEMES.LIGHT;
        this.setTheme(savedTheme);
    }

    toggleTheme() {
        const currentTheme = document.documentElement.getAttribute('data-theme');
        const newTheme = currentTheme === CONFIG.THEMES.DARK ? CONFIG.THEMES.LIGHT : CONFIG.THEMES.DARK;
        this.setTheme(newTheme);
        localStorage.setItem('theme', newTheme);
    }

    setTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        const themeToggle = document.getElementById('themeToggle');
        themeToggle.textContent = theme === CONFIG.THEMES.DARK ? '☀️' : '🌙';
    }
}
