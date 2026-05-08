/**
 * Global JavaScript Utilities for BeverageStore
 */

document.addEventListener('DOMContentLoaded', () => {
    // Check for any server-side messages embedded in the DOM
    const serverError = document.getElementById('server-error-msg');
    if (serverError && serverError.textContent.trim()) {
        showAlert(serverError.textContent.trim(), 'error');
    }

    const serverSuccess = document.getElementById('server-success-msg');
    if (serverSuccess && serverSuccess.textContent.trim()) {
        showAlert(serverSuccess.textContent.trim(), 'success');
    }

    // Check for URL parameters for login/logout redirect messages
    const urlParams = new URLSearchParams(window.location.search);
    const msg = urlParams.get('msg');
    if (msg === 'login') {
        showAlert('Successfully signed in. Welcome back!', 'success');
        window.history.replaceState({}, document.title, window.location.pathname);
    } else if (msg === 'logout') {
        showAlert('Successfully signed out.', 'success');
        window.history.replaceState({}, document.title, window.location.pathname);
    }
});

/**
 * Displays a toast notification on the screen
 * @param {string} message - The message to display
 * @param {string} type - 'success' or 'error'
 */
function showAlert(message, type = 'success') {
    // Create container if it doesn't exist
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    // Create toast element
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    const msgSpan = document.createElement('span');
    msgSpan.textContent = message;
    
    const closeBtn = document.createElement('button');
    closeBtn.className = 'toast-close';
    closeBtn.innerHTML = '&times;';
    closeBtn.onclick = () => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    };

    toast.appendChild(msgSpan);
    toast.appendChild(closeBtn);
    container.appendChild(toast);

    // Trigger animation
    setTimeout(() => {
        toast.classList.add('show');
    }, 10);

    // Auto remove after 5 seconds
    setTimeout(() => {
        if (toast.parentElement) {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }
    }, 5000);
}
