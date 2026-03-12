// employer-dashboard.js
function openStatusModal(applicationId, status, statusLabel) {
    var modal = new bootstrap.Modal(document.getElementById('statusModal'));
    document.getElementById('modalTitle').textContent = 'Move to ' + statusLabel;
    document.getElementById('targetStatus').value = status;
    document.getElementById('statusForm').action = '/employer/application/update-status/' + applicationId;
    document.getElementById('statusNotes').value = '';
    modal.show();
}

function toggleCharts() {
    var chartsSection = document.getElementById('chartsSection');
    var toggleBtn = document.getElementById('toggleChartsBtn');
    
    if (chartsSection.classList.contains('d-none')) {
        chartsSection.classList.remove('d-none');
        chartsSection.classList.add('row');
        toggleBtn.innerHTML = '<i class="bi bi-eye-slash"></i> Hide Analytics';
    } else {
        chartsSection.classList.add('d-none');
        chartsSection.classList.remove('row');
        toggleBtn.innerHTML = '<i class="bi bi-graph-up"></i> Show Analytics';
    }
}

function toggleBulkNote() {
    var status = document.getElementById("bulkStatus").value;
    var noteDiv = document.getElementById("bulkNoteDiv");
    if (status === "REJECTED" || status === "SHORTLISTED") {
        noteDiv.classList.remove("d-none");
    } else {
        noteDiv.classList.add("d-none");
    }
}

function validateBulkForm() {
    var errorBox = document.getElementById("bulkError");
    errorBox.innerText = "";
    errorBox.classList.add("d-none");

    var checkboxes = document.querySelectorAll("input[name='applicationIds']:checked");
    var status = document.getElementById("bulkStatus").value;

    if (!status) {
        showError(errorBox, "Please select an action.");
        return false;
    }

    if (checkboxes.length === 0) {
        showError(errorBox, "Please select at least one applicant.");
        return false;
    }

    var alreadyProcessed = false;
    checkboxes.forEach(function(cb) {
        var currentStatus = cb.getAttribute("data-status");
        if (currentStatus === "SHORTLISTED" || currentStatus === "REJECTED") {
            alreadyProcessed = true;
        }
    });

    if (alreadyProcessed) {
        showError(errorBox, "One or more selected applicants have already been processed (Shortlisted/Rejected).");
        return false;
    }

    if (status === "REJECTED") {
        var note = document.querySelector("textarea[name='bulkNote']").value;
        if (!note.trim()) {
            showError(errorBox, "Rejection reason is required.");
            return false;
        }
    }
    return true;
}

function showError(element, message) {
    element.innerText = message;
    element.classList.remove("d-none");
}
