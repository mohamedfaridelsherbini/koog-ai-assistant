// Configuration and global variables
const CONFIG = {
    DEFAULT_MODEL: 'deepseek-coder:6.7b',
    API_ENDPOINTS: {
        CHAT: '/api/chat',
        MODELS: '/api/models',
        MODELS_ALL: '/api/models/all',
        MODEL_PULL: '/api/models/pull',
        MODEL_DELETE: '/api/models/delete',
        MODEL_SWITCH: '/api/models/switch'
    },
    THEMES: {
        LIGHT: 'light',
        DARK: 'dark'
    }
};

// Global state
let currentModel = '';
