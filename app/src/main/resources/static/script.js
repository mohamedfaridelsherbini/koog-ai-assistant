// Enhanced Koog AI - Simple & Colorful
let currentModel = '';

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    loadModels();
    initializeTheme();
    addMessage('system', 'Hello! I\'m Koog AI. Ask me anything! 🌟');
    
    // Enter key to send
    document.getElementById('messageInput').addEventListener('keydown', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            sendMessage();
        }
    });
    
    // Model selection
    document.getElementById('modelSelect').addEventListener('change', function(e) {
        selectModel(e.target.value);
    });
    
    // Theme toggle
    document.getElementById('themeToggle').addEventListener('click', function() {
        toggleTheme();
    });
    
    // Model management
    document.getElementById('modelsBtn').addEventListener('click', function() {
        openModelModal();
    });
    
    document.getElementById('closeModal').addEventListener('click', function() {
        closeModelModal();
    });
    
    document.getElementById('pullModelBtn').addEventListener('click', function() {
        pullModel();
    });
    
    // Close modal when clicking outside
    document.getElementById('modelModal').addEventListener('click', function(e) {
        if (e.target === this) {
            closeModelModal();
        }
    });
});

// Send message
function sendMessage() {
    const input = document.getElementById('messageInput');
    const message = input.value.trim();
    
    if (!message) return;
    
    addMessage('user', message);
    input.value = '';
    
    // Show loading
    const loadingId = addLoadingMessage();
    
    // Send to AI
    fetch('/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            message: message,
            model: currentModel || 'deepseek-coder:6.7b'
        })
    })
    .then(response => response.json())
    .then(data => {
        removeMessage(loadingId);
        addMessage('assistant', data.response || 'Sorry, I couldn\'t process that.');
    })
    .catch(error => {
        removeMessage(loadingId);
        addMessage('assistant', 'Sorry, there was an error. Please try again.');
        console.error('Error:', error);
    });
}

// Add message to chat
function addMessage(type, text) {
    const messagesDiv = document.getElementById('chatMessages');
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;
    messageDiv.textContent = text;
    messagesDiv.appendChild(messageDiv);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
    return messageDiv;
}

// Add loading message
function addLoadingMessage() {
    const messagesDiv = document.getElementById('chatMessages');
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message loading';
    messageDiv.id = 'loading-' + Date.now();
    messageDiv.textContent = 'Thinking...';
    messagesDiv.appendChild(messageDiv);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
    return messageDiv.id;
}

// Remove message
function removeMessage(messageId) {
    const message = document.getElementById(messageId);
    if (message) {
        message.remove();
    }
}

// Load available models
function loadModels() {
    fetch('/api/models')
    .then(response => response.json())
    .then(data => {
        const select = document.getElementById('modelSelect');
        select.innerHTML = '<option value="">Choose Model...</option>';
        
        data.models.forEach(model => {
            const option = document.createElement('option');
            option.value = model.name;
            option.textContent = model.name;
            select.appendChild(option);
        });
        
        // Auto-select first model
        if (data.models.length > 0) {
            select.value = data.models[0].name;
            selectModel(data.models[0].name);
        }
    })
    .catch(error => {
        console.error('Error loading models:', error);
    });
}

// Select model
function selectModel(modelName) {
    currentModel = modelName;
    addMessage('system', `Switched to ${modelName} model! 🚀`);
}

// Theme functions
function initializeTheme() {
    const savedTheme = localStorage.getItem('theme') || 'light';
    setTheme(savedTheme);
}

function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    setTheme(newTheme);
    localStorage.setItem('theme', newTheme);
}

function setTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    const themeToggle = document.getElementById('themeToggle');
    themeToggle.textContent = theme === 'dark' ? '☀️' : '🌙';
}

// Model Management Functions
function openModelModal() {
    document.getElementById('modelModal').style.display = 'block';
    loadAllModels();
}

function closeModelModal() {
    document.getElementById('modelModal').style.display = 'none';
}

function loadAllModels() {
    // Load downloaded models
    fetch('/api/models/all')
    .then(response => response.json())
    .then(data => {
        displayDownloadedModels(data.downloadedModels || []);
        displayAvailableModels(data.availableModels || []);
    })
    .catch(error => {
        console.error('Error loading models:', error);
        document.getElementById('downloadedModels').innerHTML = '<div class="loading">Error loading models</div>';
        document.getElementById('availableModels').innerHTML = '<div class="loading">Error loading models</div>';
    });
}

function displayDownloadedModels(models) {
    const container = document.getElementById('downloadedModels');
    
    if (models.length === 0) {
        container.innerHTML = '<div class="loading">No models downloaded</div>';
        return;
    }
    
    container.innerHTML = models.map(model => `
        <div class="model-item">
            <div class="model-info">
                <div class="model-name">${model.name}</div>
                <div class="model-details">${model.parameterSize} • ${model.size} • ${model.quantizationLevel}</div>
            </div>
            <div class="model-actions">
                <button class="btn-success" onclick="switchToModel('${model.name}')">Use</button>
                <button class="btn-danger" onclick="deleteModel('${model.name}')">Delete</button>
            </div>
        </div>
    `).join('');
}

function displayAvailableModels(models) {
    const container = document.getElementById('availableModels');
    
    if (models.length === 0) {
        container.innerHTML = '<div class="loading">No available models</div>';
        return;
    }
    
    container.innerHTML = models.map(model => `
        <div class="model-item">
            <div class="model-info">
                <div class="model-name">${model.name}</div>
                <div class="model-details">${model.parameterSize} • ${model.downloadSize}</div>
            </div>
            <div class="model-actions">
                <button class="btn-primary" onclick="pullModel('${model.name}')">Download</button>
            </div>
        </div>
    `).join('');
}

function pullModel(modelName = null) {
    const input = document.getElementById('newModelInput');
    const modelToPull = modelName || input.value.trim();
    
    if (!modelToPull) {
        alert('Please enter a model name');
        return;
    }
    
    const pullBtn = document.getElementById('pullModelBtn');
    const progressBar = document.getElementById('pullProgress');
    
    pullBtn.disabled = true;
    pullBtn.textContent = 'Downloading...';
    progressBar.style.display = 'block';
    
    // Simulate progress
    let progress = 0;
    const progressInterval = setInterval(() => {
        progress += Math.random() * 10;
        if (progress > 90) progress = 90;
        document.querySelector('.progress-fill').style.width = progress + '%';
    }, 500);
    
    fetch('/api/models/pull', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ modelName: modelToPull })
    })
    .then(response => response.json())
    .then(data => {
        clearInterval(progressInterval);
        document.querySelector('.progress-fill').style.width = '100%';
        
        if (data.success) {
            addMessage('system', `Successfully downloaded ${modelToPull}! 🎉`);
            loadAllModels();
            loadModels(); // Refresh model dropdown
            input.value = '';
        } else {
            addMessage('system', `Failed to download ${modelToPull}: ${data.message}`);
        }
    })
    .catch(error => {
        clearInterval(progressInterval);
        addMessage('system', `Error downloading ${modelToPull}: ${error.message}`);
    })
    .finally(() => {
        pullBtn.disabled = false;
        pullBtn.textContent = 'Download';
        progressBar.style.display = 'none';
        document.querySelector('.progress-fill').style.width = '0%';
    });
}

function deleteModel(modelName) {
    if (!confirm(`Are you sure you want to delete ${modelName}?`)) {
        return;
    }
    
    fetch('/api/models/delete', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ modelName: modelName })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            addMessage('system', `Successfully deleted ${modelName}! 🗑️`);
            loadAllModels();
            loadModels(); // Refresh model dropdown
        } else {
            addMessage('system', `Failed to delete ${modelName}: ${data.message}`);
        }
    })
    .catch(error => {
        addMessage('system', `Error deleting ${modelName}: ${error.message}`);
    });
}

function switchToModel(modelName) {
    fetch('/api/models/switch', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ modelName: modelName })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            currentModel = modelName;
            document.getElementById('modelSelect').value = modelName;
            addMessage('system', `Switched to ${modelName}! 🚀`);
            closeModelModal();
        } else {
            addMessage('system', `Failed to switch to ${modelName}: ${data.message}`);
        }
    })
    .catch(error => {
        addMessage('system', `Error switching to ${modelName}: ${error.message}`);
    });
}