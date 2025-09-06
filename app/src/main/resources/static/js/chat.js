// Chat functionality
class ChatManager {
    constructor() {
        this.setupEventListeners();
    }

    setupEventListeners() {
        // Enter key to send
        document.getElementById('messageInput').addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                this.sendMessage();
            }
        });

        // Send button click
        document.getElementById('sendBtn').addEventListener('click', () => {
            this.sendMessage();
        });
    }

    sendMessage() {
        const input = document.getElementById('messageInput');
        const message = input.value.trim();
        
        if (!message) return;
        
        this.addMessage('user', message);
        input.value = '';
        
        // Add loading message directly after user message
        const loadingId = this.addLoadingMessage();
        
        // Send to AI
        fetch(CONFIG.API_ENDPOINTS.CHAT, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                message: message
            })
        })
        .then(response => response.json())
        .then(data => {
            // Replace loading message with assistant response
            this.replaceMessage(loadingId, 'assistant', data.response || 'Sorry, I couldn\'t process that.');
        })
        .catch(error => {
            // Replace loading message with error message
            this.replaceMessage(loadingId, 'assistant', 'Sorry, there was an error. Please try again.');
            console.error('Error:', error);
        });
    }

    addMessage(type, text) {
        const messagesDiv = document.getElementById('chatMessages');
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${type}`;
        
        // Create avatar
        const avatarDiv = document.createElement('div');
        avatarDiv.className = 'message-avatar';
        avatarDiv.textContent = type === 'user' ? 'U' : 'A';
        
        // Create content container
        const contentDiv = document.createElement('div');
        contentDiv.className = 'message-content';
        
        // Format text with markdown and special characters
        const formattedText = this.formatMessage(text);
        contentDiv.innerHTML = formattedText;
        
        // Create timestamp
        const timeDiv = document.createElement('div');
        timeDiv.className = 'message-time';
        timeDiv.textContent = this.getCurrentTime();
        
        // Assemble message
        messageDiv.appendChild(avatarDiv);
        messageDiv.appendChild(contentDiv);
        contentDiv.appendChild(timeDiv);
        
        messagesDiv.appendChild(messageDiv);
        messagesDiv.scrollTop = messagesDiv.scrollHeight;
        
        // Apply Prism.js syntax highlighting
        if (typeof Prism !== 'undefined') {
            Prism.highlightAllUnder(messageDiv);
        }
        
        // Update message count
        this.updateMessageCount();
        
        return messageDiv;
    }
    
    getCurrentTime() {
        const now = new Date();
        return now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }
    
    updateMessageCount() {
        const messageCount = document.getElementById('messageCount');
        const messages = document.querySelectorAll('.message');
        const count = messages.length;
        messageCount.textContent = `${count} message${count !== 1 ? 's' : ''}`;
        
        // Update last activity
        const lastActivity = document.getElementById('lastActivity');
        lastActivity.textContent = 'Just now';
    }

    addLoadingMessage() {
        const messagesDiv = document.getElementById('chatMessages');
        const messageDiv = document.createElement('div');
        messageDiv.className = 'message loading';
        messageDiv.id = 'loading-' + Date.now();
        messageDiv.textContent = 'Thinking...';
        messagesDiv.appendChild(messageDiv);
        messagesDiv.scrollTop = messagesDiv.scrollHeight;
        return messageDiv.id;
    }

    removeMessage(messageId) {
        const message = document.getElementById(messageId);
        if (message) {
            message.remove();
        }
    }

    replaceMessage(messageId, type, text) {
        const message = document.getElementById(messageId);
        if (message) {
            message.className = `message ${type}`;
            
            // Create avatar
            const avatarDiv = document.createElement('div');
            avatarDiv.className = 'message-avatar';
            avatarDiv.textContent = type === 'user' ? 'U' : 'A';
            
            // Create content container
            const contentDiv = document.createElement('div');
            contentDiv.className = 'message-content';
            
            // Format text with markdown and special characters
            const formattedText = this.formatMessage(text);
            contentDiv.innerHTML = formattedText;
            
            // Create timestamp
            const timeDiv = document.createElement('div');
            timeDiv.className = 'message-time';
            timeDiv.textContent = this.getCurrentTime();
            
            // Clear and rebuild message
            message.innerHTML = '';
            message.appendChild(avatarDiv);
            message.appendChild(contentDiv);
            contentDiv.appendChild(timeDiv);
            
            // Apply Prism.js syntax highlighting
            if (typeof Prism !== 'undefined') {
                Prism.highlightAllUnder(message);
            }
            
            // Update message count
            this.updateMessageCount();
            
            // Scroll to bottom
            const messagesDiv = document.getElementById('chatMessages');
            messagesDiv.scrollTop = messagesDiv.scrollHeight;
        }
    }

    formatMessage(text) {
        if (!text) return '';
        
        // First, unescape any escaped characters
        let formatted = text
            .replace(/\\\\n/g, '\n')  // Convert \\n to \n
            .replace(/\\n/g, '\n')    // Convert \n to \n
            .replace(/\\t/g, '\t')    // Convert \t to \t
            .replace(/\\r/g, '\r')    // Convert \r to \r
            .replace(/\\\\/g, '\\');  // Convert \\ to \
        
        // Escape HTML to prevent XSS
        formatted = formatted
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
        
        // Convert markdown formatting
        // Bold text: **text** or __text__
        formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
        formatted = formatted.replace(/__(.*?)__/g, '<strong>$1</strong>');
        
        // Italic text: *text* or _text_
        formatted = formatted.replace(/\*(.*?)\*/g, '<em>$1</em>');
        formatted = formatted.replace(/_(.*?)_/g, '<em>$1</em>');
        
        // Code blocks with language syntax: ```language\ncode\n```
        formatted = formatted.replace(/```(\w+)\n([\s\S]*?)```/g, '<pre><code class="language-$1">$2</code></pre>');
        
        // Code blocks without language: ```\ncode\n```
        formatted = formatted.replace(/```\n([\s\S]*?)```/g, '<pre><code>$1</code></pre>');
        
        // Inline code: `code`
        formatted = formatted.replace(/`(.*?)`/g, '<code>$1</code>');
        
        // Handle paragraphs - convert double line breaks to paragraph breaks
        formatted = formatted.replace(/\n\s*\n/g, '</p><p>');
        
        // Wrap in paragraph tags if not already wrapped
        if (!formatted.startsWith('<p>') && !formatted.startsWith('<ul>') && !formatted.startsWith('<ol>')) {
            formatted = '<p>' + formatted + '</p>';
        }
        
        // Line breaks: \n (single line breaks within paragraphs)
        formatted = formatted.replace(/\n/g, '<br>');
        
        // Lists: - item or * item
        formatted = formatted.replace(/^[\s]*[-*]\s+(.+)$/gm, '<li>$1</li>');
        if (formatted.includes('<li>')) {
            formatted = '<ul>' + formatted + '</ul>';
        }
        
        // Numbered lists: 1. item
        formatted = formatted.replace(/^[\s]*\d+\.\s+(.+)$/gm, '<li>$1</li>');
        if (formatted.includes('<li>') && !formatted.includes('<ul>')) {
            formatted = '<ol>' + formatted + '</ol>';
        }
        
        return formatted;
    }
}
