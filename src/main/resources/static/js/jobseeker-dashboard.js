// jobseeker-dashboard.js
document.addEventListener('DOMContentLoaded', function () {
    // Initialization code for jobseeker dashboard if needed.
    // Progress bar animation can be refined here if we want JS control instead of CSS.
    var strengthBar = document.getElementById('profileStrengthBar');
    if (strengthBar) {
        var targetWidth = strengthBar.getAttribute('aria-valuenow') + '%';
        setTimeout(function() {
            strengthBar.style.width = targetWidth;
        }, 100); // slight delay to trigger CSS transition on load
    }
});
