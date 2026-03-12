// jobseeker-saved-jobs.js
function getCsrfHeaders() {
    const tokenMeta = document.querySelector("meta[name='_csrf']");
    const headerMeta = document.querySelector("meta[name='_csrf_header']");
    if (!tokenMeta || !headerMeta) return {};
    return { [headerMeta.content]: tokenMeta.content };
}

function showNotification(message, isError = false) {
    const div = document.createElement('div');
    // Using bootstrap classes mostly, with minimal inline for positioning
    div.className = `alert alert-${isError ? 'danger' : 'success'} shadow-lg position-fixed fw-bold`;
    div.style.top = '20px';
    div.style.right = '20px';
    div.style.zIndex = '9999';
    div.style.animation = 'slideIn 0.3s ease-out';
    div.innerHTML = `<i class="bi ${isError ? 'bi-exclamation-triangle' : 'bi-check-circle'} me-2"></i>${message}`;
    document.body.appendChild(div);
    setTimeout(() => div.remove(), 3000);
}

async function removeSaved(jobId, button) {
    try {
        const response = await fetch('/jobseeker/removeSaved/' + jobId, {
            method: 'POST',
            headers: getCsrfHeaders()
        });
        if (response.ok) {
            showNotification('Removed from saved jobs');
            const card = button.closest('.job-card');
            card.style.opacity = '0';
            card.style.transform = 'translateX(20px)';
            card.style.transition = 'all 0.3s ease';
            setTimeout(() => {
                card.remove();
                if (document.querySelectorAll('.job-card').length === 0) location.reload();
            }, 300);
        }
    } catch (err) {
        showNotification('Failed to remove job', true);
    }
}
