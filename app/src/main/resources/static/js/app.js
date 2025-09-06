// Main application initialization
class App {
    constructor() {
        this.chatManager = new ChatManager();
        this.themeManager = new ThemeManager();
        this.modelManager = new ModelManager();
        
        this.initialize();
    }

    initialize() {
        this.modelManager.loadModels();
        this.themeManager.initializeTheme();
        this.chatManager.addMessage('system', 'Hello! I\'m Koog AI. Ask me anything! 🌟');
    }
}

// Global instances for cross-module access
let chatManager;
let themeManager;
let modelManager;

// Initialize app when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    const app = new App();
    
    // Make managers globally accessible for onclick handlers
    chatManager = app.chatManager;
    themeManager = app.themeManager;
    modelManager = app.modelManager;
});
