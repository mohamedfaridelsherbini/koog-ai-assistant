// Global variables
let currentModel = 'Unknown';
let memorySize = 0;
let availableModels = [];

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 DOMContentLoaded - Script is running!');
    
    // Initialize theme first
    initTheme();
    
    // Add theme toggle event listener
    const themeToggle = document.getElementById('themeToggle');
    if (themeToggle) {
        themeToggle.addEventListener('click', toggleTheme);
    }
    
    // Update memory status
    updateMemoryStatus();
    
    // Load models immediately
    loadModels();
    
    // Update model info after a short delay
    setTimeout(() => {
        updateModelInfo();
    }, 1000);
    
    addMessage('system', 'Welcome to Koog AI Assistant! I\'m ready to help you with any questions or tasks. I can also monitor system performance and provide analytics.');
    
    // Initialize Active LLM indicator
    updateActiveLLMIndicator();
});

// Chat functions
function sendMessage() {
    const input = document.getElementById('messageInput');
    const message = input.value.trim();
    
    if (message === '') return;
    
    // Update timestamp when sending message
    updateActiveLLMIndicator();
    
    // Add user message
    addMessage('user', message);
    input.value = '';
    
    // Add loading message
    const loadingId = addLoadingMessage();
    
    // Disable input while processing
    input.disabled = true;
    const sendButton = document.querySelector('.chat-input-container button');
    sendButton.disabled = true;
    
    fetch('/api/chat', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ message: message })
    })
    .then(response => response.json())
    .then(data => {
        // Remove loading message
        removeLoadingMessage(loadingId);
        
        // Add AI response
        addMessage('assistant', data.response);
        updateMemoryStatus(data.memorySize);
    })
    .catch(error => {
        // Remove loading message
        removeLoadingMessage(loadingId);
        
        // Add error message
        addMessage('error', 'Error: ' + error.message);
    })
    .finally(() => {
        // Re-enable input
        input.disabled = false;
        sendButton.disabled = false;
        input.focus();
    });
}

function addMessage(type, content) {
    const messagesDiv = document.getElementById('chatMessages');
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;
    
    const timestamp = new Date().toLocaleTimeString();
    
    messageDiv.innerHTML = `
        <div>${content}</div>
        <span class="timestamp">${timestamp}</span>
    `;
    
    messagesDiv.appendChild(messageDiv);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

function addLoadingMessage() {
    const messagesDiv = document.getElementById('chatMessages');
    const loadingDiv = document.createElement('div');
    const loadingId = 'loading-' + Date.now();
    loadingDiv.id = loadingId;
    loadingDiv.className = 'message assistant loading-message';
    
    loadingDiv.innerHTML = `
        <div class="loading-content">
            <div class="loading-dots">
                <span></span>
                <span></span>
                <span></span>
            </div>
            <span class="loading-text">Koog is thinking...</span>
        </div>
        <span class="timestamp">${new Date().toLocaleTimeString()}</span>
    `;
    
    messagesDiv.appendChild(loadingDiv);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
    
    return loadingId;
}

function removeLoadingMessage(loadingId) {
    const loadingDiv = document.getElementById(loadingId);
    if (loadingDiv) {
        loadingDiv.remove();
    }
}

// Memory functions
function clearMemory() {
    fetch('/api/memory/clear', {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        updateMemoryStatus(0);
        addMessage('system', 'Memory cleared successfully');
    })
    .catch(error => {
        addMessage('error', 'Error clearing memory: ' + error.message);
    });
}

function updateMemoryStatus(size = null) {
    if (size !== null) {
        memorySize = size;
    }
    document.getElementById('memoryStatus').textContent = `Memory: ${memorySize} messages`;
}

// Enhanced Model Management Functions
function loadModels() {
    const modelList = document.getElementById('modelList');
    
    if (!modelList) {
        console.error('modelList element not found!');
        return;
    }
    
    modelList.innerHTML = '<div class="loading">Loading all models...</div>';
    
    return fetch('/api/models/all')
        .then(response => response.json())
        .then(data => {
            try {
                // Combine downloaded and available models
                const allModels = [...data.downloadedModels, ...data.availableModels];
                availableModels = allModels;
                displayAllModels(data.downloadedModels, data.availableModels);
                // Don't call updateCurrentModelDisplay here - it will be called after updateModelInfo
            } catch (e) {
                console.error('Error in loadModels:', e);
                modelList.innerHTML = '<div class="error">Error loading models: ' + e.message + '</div>';
                throw e;
            }
        })
        .catch(error => {
            console.error('Error loading models:', error);
            modelList.innerHTML = '<div class="error">Error loading models: ' + error.message + '</div>';
            throw error;
        });
}

function displayAllModels(downloadedModels, availableModels) {
    const modelList = document.getElementById('modelList');
    
    if ((!downloadedModels || downloadedModels.length === 0) && (!availableModels || availableModels.length === 0)) {
        modelList.innerHTML = '<div class="no-models">No models available</div>';
        return;
    }
    
    modelList.innerHTML = '';
    
    // Display downloaded models first
    if (downloadedModels && downloadedModels.length > 0) {
        const downloadedSection = document.createElement('div');
        downloadedSection.className = 'model-section';
        downloadedSection.innerHTML = '<div class="model-section-title">📥 Downloaded Models</div>';
        
        downloadedModels.forEach(model => {
            const modelItem = createModelItem(model, true);
            downloadedSection.appendChild(modelItem);
        });
        
        modelList.appendChild(downloadedSection);
    }
    
    // Display available models
    if (availableModels && availableModels.length > 0) {
        const availableSection = document.createElement('div');
        availableSection.className = 'model-section';
        availableSection.innerHTML = '<div class="model-section-title">📦 Available Models</div>';
        
        availableModels.forEach(model => {
            const modelItem = createModelItem(model, false);
            availableSection.appendChild(modelItem);
        });
        
        modelList.appendChild(availableSection);
    }
}

function createModelItem(model, isDownloaded) {
    const modelItem = document.createElement('div');
    modelItem.className = `model-item ${isDownloaded ? 'downloaded' : 'available'}`;
    modelItem.setAttribute('data-model', model.name);
    
    if (isDownloaded) {
        modelItem.onclick = () => selectModel(model.name);
    } else {
        modelItem.onclick = () => pullModel(model.name);
    }
    
    const statusIcon = isDownloaded ? '✅' : '';
    const statusText = isDownloaded ? 'Downloaded' : 'Available';
    const actionButton = isDownloaded ? 
        `<button class="model-delete-btn" title="Delete ${model.name}">🗑️</button>` :
        `<button class="model-download-btn" title="Download ${model.name}">⬇️</button>`;
    
    modelItem.innerHTML = `
        <input type="radio" class="model-radio" name="model" value="${model.name}" 
               ${model.name === currentModel ? 'checked' : ''} ${!isDownloaded ? 'disabled' : ''}>
        <div class="model-info">
            <div class="model-name">${model.name} ${statusIcon}</div>
            <div class="model-details">
                ${model.parameterSize || 'Unknown'} • ${model.quantizationLevel || 'Unknown'}
                <span class="model-size">${model.size || 'Unknown'}</span>
            </div>
            <div class="model-status">${statusText}</div>
        </div>
        ${actionButton}
    `;
    
    // Add event listeners to prevent bubbling
    const downloadBtn = modelItem.querySelector('.model-download-btn');
    const deleteBtn = modelItem.querySelector('.model-delete-btn');
    
    if (downloadBtn) {
        console.log('🔧 Adding click listener to download button for:', model.name);
        downloadBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            console.log('🔥 DOWNLOAD BUTTON CLICKED!', model.name);
            alert('Download button clicked for: ' + model.name);
            try {
                pullModel(model.name);
                console.log('✅ pullModel called successfully');
            } catch (error) {
                console.error('❌ Error calling pullModel:', error);
            }
        });
    } else {
        console.error('❌ Download button not found for model:', model.name);
    }
    
    if (deleteBtn) {
        deleteBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            deleteModelFromList(model.name);
        });
    }
    
    return modelItem;
}

function displayModels(models) {
    const modelList = document.getElementById('modelList');
    
    if (!models || models.length === 0) {
        modelList.innerHTML = '<div class="no-models">No models available</div>';
        return;
    }
    
    modelList.innerHTML = '';
    
    models.forEach(model => {
        const modelItem = document.createElement('div');
        modelItem.className = 'model-item';
        modelItem.onclick = () => selectModel(model.name);
        
        const sizeInGB = (model.size / (1024 * 1024 * 1024)).toFixed(1);
        
        modelItem.innerHTML = `
            <input type="radio" class="model-radio" name="model" value="${model.name}" 
                   ${model.name === currentModel ? 'checked' : ''}>
            <div class="model-info">
                <div class="model-name">${model.name}</div>
                <div class="model-details">
                    ${model.details?.parameter_size || 'Unknown'} • ${model.details?.quantization_level || 'Unknown'}
                    <span class="model-size">${sizeInGB}GB</span>
                </div>
            </div>
            <button class="model-delete-btn" onclick="deleteModelFromList('${model.name}')" title="Delete ${model.name}">
                🗑️
            </button>
        `;
        
        modelList.appendChild(modelItem);
    });
}

function deleteModelFromList(modelName) {
    // Prevent event bubbling to avoid selecting the model
    event.stopPropagation();
    
    if (confirm(`Are you sure you want to delete ${modelName}?`)) {
        // Trigger the delete operation directly with the model name
        deleteModelDirect(modelName);
    }
}

function selectModel(modelName) {
    // Update radio button
    const radioButtons = document.querySelectorAll('.model-radio');
    radioButtons.forEach(radio => {
        radio.checked = radio.value === modelName;
    });
    
    // Update visual selection
    const modelItems = document.querySelectorAll('.model-item');
    modelItems.forEach(item => {
        item.classList.remove('selected');
        if (item.querySelector('.model-radio').value === modelName) {
            item.classList.add('selected');
        }
    });
    
    // Switch model
    switchModel(modelName);
}

function switchModel(modelName) {
    fetch('/api/models/switch', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ modelName: modelName })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            currentModel = modelName;
            updateCurrentModelDisplay();
            addMessage('system', `Model switched to ${modelName} successfully`);
            
            // Update radio button selection
            const radioButtons = document.querySelectorAll('.model-radio');
            radioButtons.forEach(radio => {
                radio.checked = radio.value === modelName;
            });
        } else {
            addMessage('error', data.message);
        }
    })
    .catch(error => {
        addMessage('error', 'Error switching model: ' + error.message);
    });
}

function refreshModels() {
    loadModels();
}

function pullModel(modelName) {
    try {
        console.log('🚀 pullModel function called with:', modelName);
        
        if (!modelName) {
            console.error('❌ No model name provided');
            addMessage('error', 'No model name provided');
            return;
        }
        
        console.log('🔍 Starting download for model:', modelName);
        
        // Find the model item and download button
        const modelItem = document.querySelector(`[data-model="${modelName}"]`);
        const downloadButton = modelItem?.querySelector('.model-download-btn');
        
        console.log('🔍 Found model item:', modelItem);
        console.log('🔍 Found download button:', downloadButton);
        console.log('🔍 Model item data-model attribute:', modelItem?.getAttribute('data-model'));
        
            if (downloadButton) {
        // Simple visual feedback
        downloadButton.innerHTML = '🔄';
        downloadButton.disabled = true;
        downloadButton.classList.add('downloading');
        
        console.log('✅ Updated download button appearance');
    }
        
        console.log('📊 Starting API call...');
        
        // Use regular fetch for now (streaming will be added later)
        fetch('/api/models/pull', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ modelName: modelName })
        })
        .then(response => {
            console.log('📡 API Response status:', response.status);
            return response.json();
        })
        .then(data => {
            console.log('📋 API Response data:', data);
            
            if (data.success) {
                addMessage('system', `✅ Model ${modelName} downloaded successfully!`);
                console.log('✅ Download completed successfully');
                
                // Show success state
                if (downloadButton) {
                    downloadButton.innerHTML = '✅ Downloaded';
                    downloadButton.classList.remove('downloading');
                    downloadButton.classList.add('success');
                    downloadButton.disabled = false;
                }
                
                if (modelItem) {
                    modelItem.classList.remove('downloading');
                    modelItem.classList.add('success');
                }
                
                // Refresh model list after showing success
                setTimeout(() => {
                    loadModels();
                }, 1000);
            } else {
                addMessage('error', 'Error downloading model: ' + data.message);
                console.log('❌ Download failed:', data.message);
                
                // Show error state
                if (downloadButton) {
                    downloadButton.innerHTML = '❌ Failed';
                    downloadButton.classList.remove('downloading');
                    downloadButton.classList.add('error');
                    downloadButton.disabled = false;
                }
                
                if (modelItem) {
                    modelItem.classList.remove('downloading');
                    
                    // Clean up progress bar
                    const progressContainer = modelItem.nextElementSibling;
                    if (progressContainer && progressContainer.classList.contains('download-progress-info')) {
                        progressContainer.remove();
                    }
                    
                    // Clear progress references
                    delete modelItem._progressContainer;
                    delete modelItem._progressFill;
                    delete modelItem._progressPercentage;
                }
                
                // Reset to normal state after error
                setTimeout(() => {
                    if (downloadButton) {
                        downloadButton.innerHTML = '⬇️';
                        downloadButton.classList.remove('error');
                    }
                }, 1500);
            }
        })
        .catch(error => {
            console.error('❌ Download error:', error);
            
            addMessage('error', 'Error downloading model: ' + error.message);
            
            // Show error state
            if (downloadButton) {
                downloadButton.innerHTML = '❌ Failed';
                downloadButton.classList.remove('downloading');
                downloadButton.classList.add('error');
                downloadButton.disabled = false;
            }
            
            if (modelItem) {
                modelItem.classList.remove('downloading');
            }
            
            // Reset to normal state after error
            setTimeout(() => {
                if (downloadButton) {
                    downloadButton.innerHTML = '⬇️';
                    downloadButton.classList.remove('error');
                }
            }, 1500);
            
            console.error('Download model error:', error);
        });
    } catch (error) {
        console.error('❌ CRITICAL ERROR in pullModel:', error);
        alert('Critical error in pullModel: ' + error.message);
    }
}


function deleteModelDirect(modelName) {
    if (!modelName) {
        addMessage('error', 'No model name provided');
        return;
    }
    
    // Show loading message
    addMessage('system', `🗑️ Deleting model ${modelName}...`);
    
    // Simulate progress for better UX
    let progress = 0;
    let currentPhase = 0;
    const phases = [
        { threshold: 30, message: '🗑️ Removing model files...', speed: 8 },
        { threshold: 70, message: '🧹 Cleaning up resources...', speed: 15 },
        { threshold: 90, message: '✅ Finalizing deletion...', speed: 10 }
    ];
    
    const progressInterval = setInterval(() => {
        if (progress >= phases[currentPhase].threshold && currentPhase < phases.length - 1) {
            currentPhase++;
            addMessage('system', phases[currentPhase].message);
        }
        
        const currentPhaseData = phases[currentPhase];
        progress += Math.random() * currentPhaseData.speed + 3;
        
        if (progress > 90) progress = 90;
    }, 800);
    
    // Add timeout to prevent infinite loading
    const timeoutId = setTimeout(() => {
        clearInterval(progressInterval);
        addMessage('error', 'Delete request timed out');
    }, 60000); // 1 minute timeout

    fetch('/api/models/delete', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ modelName: modelName })
    })
    .then(response => response.json())
    .then(data => {
        clearInterval(progressInterval);
        clearTimeout(timeoutId);
        
        if (data.success) {
            addMessage('system', `✅ Model ${modelName} deleted successfully!`);
            
            // Refresh model list immediately to update UI
            loadModels();
        } else {
            addMessage('error', 'Error deleting model: ' + data.message);
        }
    })
    .catch(error => {
        clearInterval(progressInterval);
        clearTimeout(timeoutId);
        console.error('Delete model error:', error);
        addMessage('error', 'Error deleting model: ' + error.message);
    });
}





function updateCurrentModelDisplay() {
    const currentModelDisplay = document.getElementById('currentModelDisplay');
    
    if (!currentModelDisplay) {
        console.error('currentModelDisplay element not found!');
        return;
    }
    
    const modelNameSpan = currentModelDisplay.querySelector('.model-name');
    const modelStatusSpan = currentModelDisplay.querySelector('.model-status');
    
    if (modelNameSpan) {
        modelNameSpan.textContent = currentModel;
    }
    if (modelStatusSpan) {
        modelStatusSpan.textContent = 'Active';
        modelStatusSpan.className = 'model-status';
    }
    
    // Update radio button selection to match current model
    updateModelSelection();
    
    // Update active LLM indicator
    updateActiveLLMIndicator();
}

function updateActiveLLMIndicator() {
    const activeLLMName = document.getElementById('activeLLMName');
    const llmTimestamp = document.getElementById('llmTimestamp');
    
    if (activeLLMName) {
        activeLLMName.textContent = currentModel;
    }
    
    if (llmTimestamp) {
        const now = new Date();
        const timeString = now.toLocaleTimeString('en-US', { 
            hour12: false,
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
        llmTimestamp.textContent = timeString;
    }
}

function updateModelSelection() {
    // Uncheck all radio buttons
    const radioButtons = document.querySelectorAll('input[type="radio"][name="model"]');
    radioButtons.forEach(radio => {
        radio.checked = false;
    });
    
    // Check the radio button for the current model
    const currentModelRadio = document.querySelector(`input[type="radio"][name="model"][value="${currentModel}"]`);
    if (currentModelRadio) {
        currentModelRadio.checked = true;
    }
}

function updateModelInfo() {
    return fetch('/api/models/current')
        .then(response => response.json())
        .then(data => {
            currentModel = data.currentModel;
            updateCurrentModelDisplay();
        })
        .catch(error => {
            console.error('Error getting current model:', error);
            // Set a fallback model name
            currentModel = 'llama3.1:8b';
            updateCurrentModelDisplay();
        });
}

// Analytics functions
function getStats() {
    updateAnalyticsStatus('Loading statistics...', 'loading');
    
    fetch('/api/conversation/stats')
        .then(response => response.json())
        .then(data => {
            const statsMessage = `
📊 Conversation Statistics:
• Total Messages: ${data.totalMessages}
• Session Duration: ${Math.round(data.sessionDuration / 1000)}s
• Average Response Time: ${Math.round(data.averageResponseTime)}ms
• Current Model: ${data.currentModel}
• Memory Size: ${data.memorySize} messages
            `;
            addMessage('system', statsMessage);
            updateAnalyticsStatus('Statistics loaded successfully', 'success');
        })
        .catch(error => {
            addMessage('error', 'Error getting stats: ' + error.message);
            updateAnalyticsStatus('Error loading statistics', 'error');
        });
}

function getAnalytics() {
    fetch('/api/conversation/analytics')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                addMessage('system', 'Analytics: ' + data.data);
            } else {
                addMessage('error', 'Error getting analytics: ' + data.error);
            }
        })
        .catch(error => {
            addMessage('error', 'Error getting analytics: ' + error.message);
        });
}

function exportConversation() {
    fetch('/api/conversation/export', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ format: 'json' })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            addMessage('system', 'Conversation exported successfully');
            // Create download link
            const blob = new Blob([data.data], { type: 'application/json' });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'conversation.json';
            a.click();
            URL.revokeObjectURL(url);
        } else {
            addMessage('error', 'Error exporting conversation: ' + data.data);
        }
    })
    .catch(error => {
        addMessage('error', 'Error exporting conversation: ' + error.message);
    });
}

function resetSession() {
    if (!confirm('Are you sure you want to reset the session? This will clear all conversation history.')) {
        return;
    }
    
    fetch('/api/conversation/reset', {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            document.getElementById('chatMessages').innerHTML = '';
            addMessage('system', 'Session reset successfully');
            updateMemoryStatus(0);
        } else {
            addMessage('error', 'Error resetting session: ' + data.message);
        }
    })
    .catch(error => {
        addMessage('error', 'Error resetting session: ' + error.message);
    });
}

// System monitoring functions
function getSystemMetrics() {
    fetch('/api/system/metrics')
        .then(response => response.json())
        .then(data => {
            if (data.success && data.data) {
                const metrics = data.data;
                const metricsMessage = `
🔧 System Metrics:
• CPU Usage: ${metrics.cpuUsage.toFixed(1)}%
• Memory Usage: ${metrics.memoryUsage.toFixed(1)}%
• Disk Usage: ${metrics.diskUsage.toFixed(1)}%
• Network Latency: ${metrics.networkLatency}ms
• Active Connections: ${metrics.activeConnections}
• Uptime: ${Math.round(metrics.uptime / 1000)}s
                `;
                addMessage('system', metricsMessage);
            } else {
                addMessage('error', 'Error getting system metrics: ' + (data.error || 'Unknown error'));
            }
        })
        .catch(error => {
            addMessage('error', 'Error getting system metrics: ' + error.message);
        });
}

function getPerformanceMetrics() {
    fetch('/api/system/performance')
        .then(response => response.json())
        .then(data => {
            if (data.success && data.data) {
                const perf = data.data;
                const perfMessage = `
📈 Performance Metrics:
• Average Response Time: ${perf.averageResponseTime.toFixed(0)}ms
• Total Requests: ${perf.totalRequests}
• Successful Requests: ${perf.successfulRequests}
• Failed Requests: ${perf.failedRequests}
• Throughput: ${perf.throughput.toFixed(2)} req/min
• Error Rate: ${(perf.errorRate * 100).toFixed(2)}%
                `;
                addMessage('system', perfMessage);
            } else {
                addMessage('error', 'Error getting performance metrics: ' + (data.error || 'Unknown error'));
            }
        })
        .catch(error => {
            addMessage('error', 'Error getting performance metrics: ' + error.message);
        });
}

function getSystemHealth() {
    fetch('/api/system/health')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                addMessage('system', 'System Health: ' + data.data);
            } else {
                addMessage('error', 'Error getting system health: ' + (data.error || 'Unknown error'));
            }
        })
        .catch(error => {
            addMessage('error', 'Error getting system health: ' + error.message);
        });
}

// File operation functions
function readFile() {
    const filename = document.getElementById('filenameInput').value.trim();
    if (!filename) {
        alert('Please enter a filename');
        return;
    }
    
    fetch('/api/files/operation', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ operation: 'read', filename: filename })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            addMessage('system', `File "${filename}" content:\n${data.data}`);
        } else {
            addMessage('error', 'Error reading file: ' + data.message);
        }
    })
    .catch(error => {
        addMessage('error', 'Error reading file: ' + error.message);
    });
}

function writeFile() {
    const filename = document.getElementById('writeFilenameInput').value.trim();
    const content = document.getElementById('contentInput').value.trim();
    
    if (!filename || !content) {
        alert('Please enter both filename and content');
        return;
    }
    
    fetch('/api/files/operation', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ operation: 'write', filename: filename, content: content })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            addMessage('system', `File "${filename}" written successfully`);
            document.getElementById('writeFilenameInput').value = '';
            document.getElementById('contentInput').value = '';
        } else {
            addMessage('error', 'Error writing file: ' + data.message);
        }
    })
    .catch(error => {
        addMessage('error', 'Error writing file: ' + error.message);
    });
}

function listFiles() {
    fetch('/api/files/operation', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ operation: 'list' })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            addMessage('system', `Files in directory:\n${data.data}`);
        } else {
            addMessage('error', 'Error listing files: ' + data.message);
        }
    })
    .catch(error => {
        addMessage('error', 'Error listing files: ' + error.message);
    });
}

// Status update functions
function updateAnalyticsStatus(message, type = 'info') {
    const statusElement = document.getElementById('analyticsStatus');
    if (statusElement) {
        statusElement.textContent = message;
        statusElement.className = `analytics-status status-${type}`;
    }
}

function updateSystemStatus(message, type = 'info') {
    const statusElement = document.getElementById('systemStatus');
    if (statusElement) {
        statusElement.textContent = message;
        statusElement.className = `system-status status-${type}`;
    }
}

function updateFileStatus(message, type = 'info') {
    const statusElement = document.getElementById('fileStatus');
    if (statusElement) {
        statusElement.textContent = message;
        statusElement.className = `file-status status-${type}`;
    }
}

function updateHealthIndicator(status) {
    const healthDot = document.querySelector('.health-dot');
    const healthText = document.querySelector('.health-text');
    const healthIndicator = document.getElementById('healthIndicator');
    
    if (healthDot && healthText && healthIndicator) {
        const statusColors = {
            'healthy': '#28a745',
            'warning': '#ffc107',
            'critical': '#dc3545'
        };
        
        healthDot.style.background = statusColors[status] || '#28a745';
        healthText.textContent = `System Status: ${status.toUpperCase()}`;
        healthIndicator.style.borderLeftColor = statusColors[status] || '#28a745';
    }
}

// Enhanced system monitoring functions
function getSystemMetrics() {
    updateSystemStatus('Loading system metrics...', 'loading');
    
    fetch('/api/system/metrics')
        .then(response => response.json())
        .then(data => {
            if (data.success && data.data) {
                const metrics = data.data;
                const metricsMessage = `
💻 System Metrics:
• CPU Usage: ${metrics.cpuUsage.toFixed(1)}%
• Memory Usage: ${metrics.memoryUsage.toFixed(1)}%
• Disk Usage: ${metrics.diskUsage.toFixed(1)}%
• Network Latency: ${metrics.networkLatency}ms
• Active Connections: ${metrics.activeConnections}
• Uptime: ${Math.round(metrics.uptime / 1000)}s
                `;
                addMessage('system', metricsMessage);
                updateSystemStatus('System metrics loaded successfully', 'success');
                updateHealthIndicator('healthy');
            } else {
                addMessage('error', 'Error getting system metrics: ' + (data.error || 'Unknown error'));
                updateSystemStatus('Failed to load system metrics', 'error');
                updateHealthIndicator('critical');
            }
        })
        .catch(error => {
            addMessage('error', 'Error getting system metrics: ' + error.message);
            updateSystemStatus('Error loading system metrics', 'error');
            updateHealthIndicator('critical');
        });
}

function getPerformanceMetrics() {
    updateSystemStatus('Loading performance metrics...', 'loading');
    
    fetch('/api/system/performance')
        .then(response => response.json())
        .then(data => {
            if (data.success && data.data) {
                const perf = data.data;
                const perfMessage = `
📈 Performance Metrics:
• Average Response Time: ${perf.averageResponseTime.toFixed(0)}ms
• Total Requests: ${perf.totalRequests}
• Successful Requests: ${perf.successfulRequests}
• Failed Requests: ${perf.failedRequests}
• Throughput: ${perf.throughput.toFixed(2)} req/min
• Error Rate: ${(perf.errorRate * 100).toFixed(2)}%
                `;
                addMessage('system', perfMessage);
                updateSystemStatus('Performance metrics loaded successfully', 'success');
            } else {
                addMessage('error', 'Error getting performance metrics: ' + (data.error || 'Unknown error'));
                updateSystemStatus('Failed to load performance metrics', 'error');
            }
        })
        .catch(error => {
            addMessage('error', 'Error getting performance metrics: ' + error.message);
            updateSystemStatus('Error loading performance metrics', 'error');
        });
}

function getSystemHealth() {
    updateSystemStatus('Checking system health...', 'loading');
    
    fetch('/api/system/health')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                addMessage('system', 'System Health: ' + data.data);
                updateSystemStatus('System health check completed', 'success');
                updateHealthIndicator('healthy');
            } else {
                addMessage('error', 'Error getting system health: ' + (data.error || 'Unknown error'));
                updateSystemStatus('System health check failed', 'error');
                updateHealthIndicator('critical');
            }
        })
        .catch(error => {
            addMessage('error', 'Error getting system health: ' + error.message);
            updateSystemStatus('System health check error', 'error');
            updateHealthIndicator('critical');
        });
}

// Enhanced file operation functions
function readFile() {
    const filename = document.getElementById('filenameInput').value.trim();
    if (!filename) {
        updateFileStatus('Please enter a filename', 'error');
        return;
    }
    
    updateFileStatus('Reading file...', 'loading');
    
    fetch('/api/files/operation', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ operation: 'read', filename: filename })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            addMessage('system', `File "${filename}" content:\n${data.data}`);
            updateFileStatus('File read successfully', 'success');
        } else {
            addMessage('error', 'Error reading file: ' + data.message);
            updateFileStatus('Error reading file', 'error');
        }
    })
    .catch(error => {
        addMessage('error', 'Error reading file: ' + error.message);
        updateFileStatus('Error reading file', 'error');
    });
}

function writeFile() {
    const filename = document.getElementById('writeFilenameInput').value.trim();
    const content = document.getElementById('contentInput').value.trim();
    
    if (!filename || !content) {
        updateFileStatus('Please enter both filename and content', 'error');
        return;
    }
    
    updateFileStatus('Writing file...', 'loading');
    
    fetch('/api/files/operation', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ operation: 'write', filename: filename, content: content })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            addMessage('system', `File "${filename}" written successfully`);
            document.getElementById('writeFilenameInput').value = '';
            document.getElementById('contentInput').value = '';
            updateFileStatus('File written successfully', 'success');
        } else {
            addMessage('error', 'Error writing file: ' + data.message);
            updateFileStatus('Error writing file', 'error');
        }
    })
    .catch(error => {
        addMessage('error', 'Error writing file: ' + error.message);
        updateFileStatus('Error writing file', 'error');
    });
}

function listFiles() {
    updateFileStatus('Listing files...', 'loading');
    
    fetch('/api/files/operation', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ operation: 'list' })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            addMessage('system', `Files in directory:\n${data.data}`);
            updateFileStatus('Files listed successfully', 'success');
        } else {
            addMessage('error', 'Error listing files: ' + data.message);
            updateFileStatus('Error listing files', 'error');
        }
    })
    .catch(error => {
        addMessage('error', 'Error listing files: ' + error.message);
        updateFileStatus('Error listing files', 'error');
    });
}

// Keyboard shortcuts
document.addEventListener('keydown', function(event) {
    if (event.key === 'Enter' && event.target.id === 'messageInput') {
        sendMessage();
    }
});

// Theme management
let currentTheme = 'system';

function initTheme() {
    // Get saved theme or default to system
    const savedTheme = localStorage.getItem('theme');
    currentTheme = savedTheme || 'system';
    
    // Apply theme
    applyTheme(currentTheme);
    
    // Update UI
    updateThemeToggle();
    
    // Listen for system theme changes
    if (currentTheme === 'system') {
        const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
        mediaQuery.addEventListener('change', handleSystemThemeChange);
    }
}

function applyTheme(theme) {
    const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    const wantDark = theme === 'dark' || (theme === 'system' && systemDark);
    
    if (wantDark) {
        document.documentElement.classList.add('dark');
        document.documentElement.setAttribute('data-theme', 'dark');
    } else {
        document.documentElement.classList.remove('dark');
        document.documentElement.setAttribute('data-theme', 'light');
    }
}

function handleSystemThemeChange() {
    if (currentTheme === 'system') {
        applyTheme('system');
    }
}

function setTheme(theme) {
    currentTheme = theme;
    localStorage.setItem('theme', theme);
    applyTheme(theme);
    updateThemeToggle();
    
    // Update system theme listener
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    if (theme === 'system') {
        mediaQuery.addEventListener('change', handleSystemThemeChange);
    } else {
        mediaQuery.removeEventListener('change', handleSystemThemeChange);
    }
}

function updateThemeToggle() {
    const themeSelect = document.getElementById('themeSelect');
    if (themeSelect) {
        themeSelect.value = currentTheme;
    }
}

// Theme select event listener
document.addEventListener('DOMContentLoaded', function() {
    const themeSelect = document.getElementById('themeSelect');
    if (themeSelect) {
        themeSelect.addEventListener('change', function(e) {
            setTheme(e.target.value);
        });
    }
});

// Theme initialization is now handled in the main DOMContentLoaded event listener above

