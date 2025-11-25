/**
 * Car Builder VIN System - JavaScript Application
 * HTMX integration and Jarvis theme interactions
 */

// Initialize the application when the DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚗 Car Builder VIN System - Jarvis Interface Initialized');
    
    // Initialize components
    initSystemStatus();
    initSearchFunctionality();
    initNotifications();
    initAnimations();
});

/**
 * System Status Management
 */
function initSystemStatus() {
    const statusElements = document.querySelectorAll('.system-status');
    
    statusElements.forEach(element => {
        // Add pulse animation to status indicators
        const indicator = element.querySelector('.status-indicator');
        if (indicator) {
            // Randomize pulse timing for more realistic effect
            indicator.style.animationDelay = Math.random() * 2 + 's';
        }
    });
    
    // Simulate system status updates (for demo)
    setInterval(updateSystemMetrics, 5000);
}

function updateSystemMetrics() {
    const metricsElements = document.querySelectorAll('[data-metric]');
    
    metricsElements.forEach(element => {
        const metric = element.getAttribute('data-metric');
        const currentValue = parseInt(element.textContent) || 0;
        
        // Simulate small variations in metrics
        let newValue = currentValue;
        switch(metric) {
            case 'cpu':
                newValue = Math.max(0, Math.min(100, currentValue + (Math.random() - 0.5) * 10));
                break;
            case 'memory':
                newValue = Math.max(0, Math.min(100, currentValue + (Math.random() - 0.5) * 5));
                break;
            case 'vehicles':
                // Keep vehicle count stable
                break;
        }
        
        if (newValue !== currentValue && metric !== 'vehicles') {
            element.textContent = Math.round(newValue);
            element.classList.add('fade-in');
            setTimeout(() => element.classList.remove('fade-in'), 500);
        }
    });
}

/**
 * Search Functionality with HTMX
 */
function initSearchFunctionality() {
    const searchInputs = document.querySelectorAll('input[type="search"]');
    
    searchInputs.forEach(input => {
        // Add loading indicator functionality
        input.addEventListener('htmx:beforeRequest', function() {
            this.classList.add('loading');
        });
        
        input.addEventListener('htmx:afterRequest', function() {
            this.classList.remove('loading');
        });
        
        // Add enhanced visual feedback
        input.addEventListener('focus', function() {
            this.parentElement.style.boxShadow = '0 0 15px rgba(0, 191, 255, 0.3)';
        });
        
        input.addEventListener('blur', function() {
            this.parentElement.style.boxShadow = '';
        });
    });
}

/**
 * Notification System
 */
function initNotifications() {
    window.showNotification = function(message, type = 'info', duration = 3000) {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.innerHTML = `
            <span class="notification-message">${message}</span>
            <button class="notification-close" onclick="this.parentElement.remove()">×</button>
        `;
        
        // Add notification styles if not already in CSS
        if (!document.querySelector('#notification-styles')) {
            const styles = document.createElement('style');
            styles.id = 'notification-styles';
            styles.textContent = `
                .notification {
                    position: fixed;
                    top: 20px;
                    right: 20px;
                    padding: 1rem 1.5rem;
                    background: var(--card-background-color);
                    border: 1px solid var(--card-border-color);
                    border-radius: var(--border-radius);
                    box-shadow: var(--card-box-shadow);
                    z-index: 1000;
                    display: flex;
                    align-items: center;
                    gap: 1rem;
                    max-width: 400px;
                    animation: slideInRight 0.3s ease;
                }
                
                .notification-info { border-left: 4px solid var(--jarvis-glow-color); }
                .notification-success { border-left: 4px solid var(--jarvis-success-color); }
                .notification-warning { border-left: 4px solid var(--jarvis-warning-color); }
                .notification-error { border-left: 4px solid var(--jarvis-danger-color); }
                
                .notification-close {
                    background: none;
                    border: none;
                    color: var(--muted-color);
                    cursor: pointer;
                    font-size: 1.2rem;
                    padding: 0;
                    line-height: 1;
                }
                
                .notification-close:hover {
                    color: var(--color);
                }
                
                @keyframes slideInRight {
                    from { transform: translateX(100%); opacity: 0; }
                    to { transform: translateX(0); opacity: 1; }
                }
            `;
            document.head.appendChild(styles);
        }
        
        document.body.appendChild(notification);
        
        // Auto-remove after duration
        if (duration > 0) {
            setTimeout(() => {
                if (notification.parentElement) {
                    notification.style.animation = 'slideInRight 0.3s ease reverse';
                    setTimeout(() => notification.remove(), 300);
                }
            }, duration);
        }
    };
}

/**
 * Animation and Visual Effects
 */
function initAnimations() {
    // Intersection Observer for fade-in animations
    const observerOptions = {
        root: null,
        rootMargin: '0px',
        threshold: 0.1
    };
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('fade-in');
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);
    
    // Observe elements with animation classes
    document.querySelectorAll('.animate-on-scroll').forEach(el => {
        observer.observe(el);
    });
    
    // Add hover effects to cards
    document.querySelectorAll('.jarvis-card, .module-card').forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-2px)';
            this.style.boxShadow = '0 8px 25px rgba(0, 191, 255, 0.15)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = '';
            this.style.boxShadow = '';
        });
    });
}

/**
 * HTMX Event Handlers
 */

// Global HTMX event listeners
document.addEventListener('htmx:beforeRequest', function(event) {
    console.log('🔄 HTMX Request:', event.detail.xhr.responseURL);
    
    // Add global loading indicator
    const loadingIndicator = document.querySelector('.global-loading');
    if (loadingIndicator) {
        loadingIndicator.style.display = 'block';
    }
});

document.addEventListener('htmx:afterRequest', function(event) {
    console.log('✅ HTMX Response:', event.detail.xhr.status);
    
    // Remove global loading indicator
    const loadingIndicator = document.querySelector('.global-loading');
    if (loadingIndicator) {
        loadingIndicator.style.display = 'none';
    }
    
    // Re-initialize components for dynamically loaded content
    initAnimations();
});

document.addEventListener('htmx:responseError', function(event) {
    console.error('❌ HTMX Error:', event.detail);
    showNotification('System communication error. Please try again.', 'error');
});

document.addEventListener('htmx:timeout', function(event) {
    console.warn('⏱️ HTMX Timeout:', event.detail);
    showNotification('Request timeout. Please check your connection.', 'warning');
});

/**
 * Utility Functions
 */

// Format numbers with appropriate suffixes
function formatNumber(num) {
    if (num >= 1000000) {
        return (num / 1000000).toFixed(1) + 'M';
    }
    if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'K';
    }
    return num.toString();
}

// Format currency
function formatCurrency(amount, currency = 'USD') {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: currency
    }).format(amount);
}

// Debounce function for search inputs
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Theme toggle function (for future use)
function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    
    showNotification(`Switched to ${newTheme} mode`, 'info');
}

// Initialize saved theme
const savedTheme = localStorage.getItem('theme') || 'dark';
document.documentElement.setAttribute('data-theme', savedTheme);

// Expose utility functions globally
window.CarBuilderVIN = {
    showNotification,
    formatNumber,
    formatCurrency,
    toggleTheme,
    debounce
};