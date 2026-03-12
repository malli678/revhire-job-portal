// js/employer-applicants.js
// Logic for applicants.html filtering and modals using Bootstrap 5

let bsNoteModal;
let bsStatusModal;

document.addEventListener('DOMContentLoaded', () => {
    // Initialize modals
    const noteModalEl = document.getElementById('noteModal');
    if (noteModalEl) bsNoteModal = new bootstrap.Modal(noteModalEl);

    const statusModalEl = document.getElementById('statusModal');
    if (statusModalEl) bsStatusModal = new bootstrap.Modal(statusModalEl);

    updateBulkUI();
});

function applyFilters() {
    const skill = (document.getElementById('skillFilter').value || '').toLowerCase();
    const exp = parseInt(document.getElementById('expFilter').value) || 0;
    const edu = (document.getElementById('eduFilter').value || '').toLowerCase();
    const status = document.getElementById('statusFilter').value;

    const cards = document.querySelectorAll('.candidate-card-wrapper');
    cards.forEach(card => {
        const cardSkills = card.getAttribute('data-skills') || '';
        const cardExp = parseInt(card.getAttribute('data-exp')) || 0;
        const cardEdu = (card.getAttribute('data-edu') || '').toLowerCase();
        const cardStatus = card.getAttribute('data-status') || '';

        let show = true;
        if (skill && !cardSkills.includes(skill)) show = false;
        if (exp && cardExp < exp) show = false;
        if (edu && !cardEdu.includes(edu)) show = false;
        if (status !== 'All Status' && cardStatus !== status) show = false;

        if (show) {
            card.classList.remove('d-none');
        } else {
            card.classList.add('d-none');
        }
    });
}

function resetFilters() {
    document.getElementById('skillFilter').value = '';
    document.getElementById('expFilter').value = '';
    document.getElementById('eduFilter').value = '';
    document.getElementById('statusFilter').value = 'All Status';
    
    const cards = document.querySelectorAll('.candidate-card-wrapper');
    cards.forEach(card => card.classList.remove('d-none'));
}

// Bulk update mock implementations (currently unused in UI unless checkboxes added in future)
function updateBulkUI() {
    const checkboxes = document.querySelectorAll('.app-checkbox:checked');
    const count = checkboxes.length;
    const bulkActions = document.getElementById('bulkActions');
    const selectedCount = document.getElementById('selectedCount');

    if (bulkActions) {
        if (count > 0) {
            bulkActions.classList.remove('d-none');
            bulkActions.classList.add('d-flex');
            selectedCount.textContent = count;
        } else {
            bulkActions.classList.add('d-none');
            bulkActions.classList.remove('d-flex');
        }
    }
}

function clearSelection() {
    document.querySelectorAll('.app-checkbox').forEach(cb => cb.checked = false);
    updateBulkUI();
}

function openBulkStatusModal(status) {
    const checkboxes = document.querySelectorAll('.app-checkbox:checked');
    const ids = Array.from(checkboxes).map(cb => cb.value).join(',');

    document.getElementById('statusModalLabel').innerHTML = `<i class="bi bi-stack text-primary me-2"></i> Bulk Update to ${status}`;
    document.getElementById('targetStatus').value = status;
    document.getElementById('statusForm').action = '/employer/application/bulk-update?ids=' + ids;
    document.getElementById('statusNotes').value = '';
    
    if (bsStatusModal) bsStatusModal.show();
}

function openStatusModal(applicationId, currentStatus) {
    document.getElementById('statusModalLabel').innerHTML = `<i class="bi bi-arrow-repeat text-primary me-2"></i> Update Status`;
    if (currentStatus) {
        document.getElementById('targetStatus').value = currentStatus;
    } else {
        document.getElementById('targetStatus').value = ""; // Reset to default
    }
    document.getElementById('statusForm').action = '/employer/application/update-status/' + applicationId;
    document.getElementById('statusNotes').value = '';
    
    if (bsStatusModal) bsStatusModal.show();
}

function openNoteModal(applicationId) {
    document.getElementById('noteApplicationId').value = applicationId;
    document.getElementById('noteForm').action = '/applications/add-notes';
    document.getElementById('noteContent').value = '';
    
    if (bsNoteModal) bsNoteModal.show();
}
