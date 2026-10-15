// Fitness Equipment Maintenance Portal - Frontend Interactions

let currentRole = localStorage.getItem('fitnessPortalRole') || 'TECHNICIAN';

document.addEventListener('DOMContentLoaded', () => {
    updateRoleDisplay();

    // Setup role selector if present
    const roleSelector = document.getElementById('roleSelector');
    if (roleSelector) {
        roleSelector.value = currentRole;
        roleSelector.addEventListener('change', (e) => {
            currentRole = e.target.value;
            localStorage.setItem('fitnessPortalRole', currentRole);
            updateRoleDisplay();
        });
    }

    // Modal triggers
    const logBreakdownBtn = document.getElementById('btnOpenReportModal');
    if (logBreakdownBtn) {
        logBreakdownBtn.addEventListener('click', () => openModal('reportModal'));
    }

    const addEquipmentBtn = document.getElementById('btnOpenAddEquipmentModal');
    if (addEquipmentBtn) {
        addEquipmentBtn.addEventListener('click', () => openModal('equipmentModal'));
    }

    // Attach form submit handlers
    const reportForm = document.getElementById('reportBreakdownForm');
    if (reportForm) {
        reportForm.addEventListener('submit', handleReportSubmit);
    }

    const equipmentForm = document.getElementById('addEquipmentForm');
    if (equipmentForm) {
        equipmentForm.addEventListener('submit', handleEquipmentSubmit);
    }

    const workflowForm = document.getElementById('updateWorkflowForm');
    if (workflowForm) {
        workflowForm.addEventListener('submit', handleWorkflowSubmit);
    }

    // Delegated click handler for workflow transitions
    document.addEventListener('click', (e) => {
        const btn = e.target.closest('.btn-workflow-trigger');
        if (btn) {
            const ticketId = btn.getAttribute('data-ticket-id');
            const status = btn.getAttribute('data-status');
            const tech = btn.getAttribute('data-technician');
            openWorkflowModal(ticketId, status, tech);
        }
    });
});

function updateRoleDisplay() {
    const roleBadge = document.getElementById('currentRoleBadge');
    if (roleBadge) {
        roleBadge.textContent = 'Active Role: ' + currentRole.replace('_', ' ');
    }
}

function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('active');
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('active');
    }
}

async function handleReportSubmit(e) {
    e.preventDefault();
    const payload = {
        equipmentId: parseInt(document.getElementById('reportEquipmentId').value),
        reportedBy: document.getElementById('reportedByName').value,
        reporterRole: currentRole,
        issueTitle: document.getElementById('reportIssueTitle').value,
        issueDescription: document.getElementById('reportIssueDesc').value,
        priority: document.getElementById('reportPriority').value,
        status: 'REPORTED'
    };

    try {
        const res = await fetch('/api/maintenance', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            alert('Maintenance breakdown successfully reported! Assigned ID and queued.');
            window.location.reload();
        } else {
            const err = await res.json();
            alert('Error reporting breakdown: ' + (err.message || 'Validation failed'));
        }
    } catch (err) {
        console.error(err);
        alert('Network or server error while submitting breakdown.');
    }
}

async function handleEquipmentSubmit(e) {
    e.preventDefault();
    const payload = {
        name: document.getElementById('eqName').value,
        type: document.getElementById('eqType').value,
        serialNumber: document.getElementById('eqSerial').value,
        gymZone: document.getElementById('eqZone').value,
        manufacturer: document.getElementById('eqManufacturer').value,
        modelNumber: document.getElementById('eqModel').value,
        installationDate: document.getElementById('eqInstallDate').value || new Date().toISOString().split('T')[0],
        currentStatus: 'OPERATIONAL'
    };

    try {
        const res = await fetch('/api/equipment', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            alert('Equipment registered successfully!');
            window.location.reload();
        } else {
            alert('Failed to register equipment. Please verify serial number is unique.');
        }
    } catch (err) {
        console.error(err);
        alert('Network error while saving equipment.');
    }
}

function openWorkflowModal(ticketId, currentStatus, currentTech) {
    document.getElementById('wfTicketId').value = ticketId;
    document.getElementById('wfNextStatus').value = currentStatus;
    document.getElementById('wfTechnician').value = currentTech || '';
    openModal('workflowModal');
}

async function handleWorkflowSubmit(e) {
    e.preventDefault();
    const ticketId = document.getElementById('wfTicketId').value;
    const payload = {
        status: document.getElementById('wfNextStatus').value,
        role: currentRole,
        technician: document.getElementById('wfTechnician').value,
        notes: document.getElementById('wfNotes').value,
        actualCost: parseFloat(document.getElementById('wfActualCost').value) || 0.0
    };

    try {
        const res = await fetch(`/api/maintenance/${ticketId}/workflow`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            alert('Workflow transition applied successfully!');
            window.location.reload();
        } else {
            const data = await res.json();
            alert('Workflow Error: ' + (data.error || 'Access denied for current role'));
        }
    } catch (err) {
        console.error(err);
        alert('Server communication error.');
    }
}

async function deleteEquipmentItem(id) {
    if (!confirm('Are you sure you want to delete this equipment item and its logs?')) {
        return;
    }
    try {
        const res = await fetch(`/api/equipment/${id}`, { method: 'DELETE' });
        if (res.ok) {
            window.location.reload();
        } else {
            alert('Could not delete equipment.');
        }
    } catch (err) {
        alert('Error deleting equipment.');
    }
}
