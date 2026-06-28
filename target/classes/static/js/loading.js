document.addEventListener("DOMContentLoaded", () => {

    const form = document.getElementById("analyze-form");

    if (!form) {
        return;
    }

    form.addEventListener("submit", () => {

        const overlay = document.getElementById("loadingOverlay");

        overlay.classList.remove("d-none");
        overlay.classList.add("d-flex");

        const submitButton = document.getElementById("submitButton");

        if (submitButton) {

            submitButton.disabled = true;

            submitButton.innerHTML = `
                <span class="spinner-border spinner-border-sm me-2"></span>
                Analyse...
            `;
        }

    });

});