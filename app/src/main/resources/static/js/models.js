// Model management
class ModelManager {
    constructor() {
        this.setupEventListeners();
    }

    setupEventListeners() {
        // Model selection
        document.getElementById('modelSelect').addEventListener('change', (e) => {
            this.selectModel(e.target.value);
        });

        // Model management
        document.getElementById('modelsBtn').addEventListener('click', () => {
            this.openModelModal();
        });

        document.getElementById('closeModal').addEventListener('click', () => {
            this.closeModelModal();
        });

        document.getElementById('pullModelBtn').addEventListener('click', () => {
            this.pullModel();
        });

        // Close modal when clicking outside
        document.getElementById('modelModal').addEventListener('click', (e) => {
            if (e.target === this) {
                this.closeModelModal();
            }
        });
    }

    loadModels() {
        fetch(CONFIG.API_ENDPOINTS.MODELS)
        .then(response => response.json())
        .then(data => {
            const select = document.getElementById('modelSelect');
            select.innerHTML = '<option value="">Choose Model...</option>';
            
            data.models.forEach(model => {
                const option = document.createElement('option');
                option.value = model;
                option.textContent = model;
                select.appendChild(option);
            });
            
            // Auto-select first model
            if (data.models.length > 0) {
                select.value = data.models[0];
                this.selectModel(data.models[0]);
            }
        })
        .catch(error => {
            console.error('Error loading models:', error);
        });
    }

    selectModel(modelName) {
        currentModel = modelName;
        chatManager.addMessage('system', `Switched to ${modelName} model! 🚀`);
    }

    openModelModal() {
        document.getElementById('modelModal').style.display = 'block';
        this.loadAllModels();
    }

    closeModelModal() {
        document.getElementById('modelModal').style.display = 'none';
    }

    loadAllModels() {
        fetch(CONFIG.API_ENDPOINTS.MODELS_ALL)
        .then(response => response.json())
        .then(data => {
            this.displayDownloadedModels(data.downloadedModels || []);
            this.displayAvailableModels(data.availableModels || []);
        })
        .catch(error => {
            console.error('Error loading models:', error);
            document.getElementById('downloadedModels').innerHTML = '<div class="loading">Error loading models</div>';
            document.getElementById('availableModels').innerHTML = '<div class="loading">Error loading models</div>';
        });
    }

    displayDownloadedModels(models) {
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
                    <button class="btn-success" onclick="modelManager.switchToModel('${model.name}')">Use</button>
                    <button class="btn-danger" onclick="modelManager.deleteModel('${model.name}')">Delete</button>
                </div>
            </div>
        `).join('');
    }

    displayAvailableModels(models) {
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
                    <button class="btn-primary" onclick="modelManager.pullModel('${model.name}')">Download</button>
                </div>
            </div>
        `).join('');
    }

    pullModel(modelName = null) {
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
        
        fetch(CONFIG.API_ENDPOINTS.MODEL_PULL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ modelName: modelToPull })
        })
        .then(response => response.json())
        .then(data => {
            clearInterval(progressInterval);
            document.querySelector('.progress-fill').style.width = '100%';
            
            if (data.success) {
                chatManager.addMessage('system', `Successfully downloaded ${modelToPull}! 🎉`);
                this.loadAllModels();
                this.loadModels(); // Refresh model dropdown
                input.value = '';
            } else {
                chatManager.addMessage('system', `Failed to download ${modelToPull}: ${data.message}`);
            }
        })
        .catch(error => {
            clearInterval(progressInterval);
            chatManager.addMessage('system', `Error downloading ${modelToPull}: ${error.message}`);
        })
        .finally(() => {
            pullBtn.disabled = false;
            pullBtn.textContent = 'Download';
            progressBar.style.display = 'none';
            document.querySelector('.progress-fill').style.width = '0%';
        });
    }

    deleteModel(modelName) {
        if (!confirm(`Are you sure you want to delete ${modelName}?`)) {
            return;
        }
        
        fetch(CONFIG.API_ENDPOINTS.MODEL_DELETE, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ modelName: modelName })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                chatManager.addMessage('system', `Successfully deleted ${modelName}! 🗑️`);
                this.loadAllModels();
                this.loadModels(); // Refresh model dropdown
            } else {
                chatManager.addMessage('system', `Failed to delete ${modelName}: ${data.message}`);
            }
        })
        .catch(error => {
            chatManager.addMessage('system', `Error deleting ${modelName}: ${error.message}`);
        });
    }

    switchToModel(modelName) {
        fetch(CONFIG.API_ENDPOINTS.MODEL_SWITCH, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ modelName: modelName })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                currentModel = modelName;
                document.getElementById('modelSelect').value = modelName;
                chatManager.addMessage('system', `Switched to ${modelName}! 🚀`);
                this.closeModelModal();
            } else {
                chatManager.addMessage('system', `Failed to switch to ${modelName}: ${data.message}`);
            }
        })
        .catch(error => {
            chatManager.addMessage('system', `Error switching to ${modelName}: ${error.message}`);
        });
    }
}
