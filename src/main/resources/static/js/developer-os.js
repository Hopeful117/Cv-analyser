document.addEventListener("DOMContentLoaded", () => {
    initializeNavigation();
    initializeLoadingForms();
    initializeCopyButtons();
});

function initializeNavigation() {
    const sidebar = document.getElementById("appSidebar");
    const backdrop = document.getElementById("sidebarBackdrop");
    const toggle = document.getElementById("mobileNavToggle");

    if (!sidebar || !backdrop || !toggle) {
        return;
    }

    const closeNavigation = () => {
        sidebar.classList.remove("is-open");
        backdrop.classList.remove("is-visible");
        document.body.classList.remove("nav-open");
        toggle.setAttribute("aria-expanded", "false");
    };

    toggle.addEventListener("click", () => {
        const willOpen = !sidebar.classList.contains("is-open");
        sidebar.classList.toggle("is-open", willOpen);
        backdrop.classList.toggle("is-visible", willOpen);
        document.body.classList.toggle("nav-open", willOpen);
        toggle.setAttribute("aria-expanded", String(willOpen));
    });

    backdrop.addEventListener("click", closeNavigation);
    document.addEventListener("keydown", event => {
        if (event.key === "Escape") {
            closeNavigation();
        }
    });
}

function initializeLoadingForms() {
    document.querySelectorAll("[data-loading-form]").forEach(form => {
        form.addEventListener("submit", () => {
            const loaderId = form.dataset.loaderTarget || "loadingOverlay";
            const loader = document.getElementById(loaderId);
            const submitButton = form.querySelector("[type='submit']");

            if (loader) {
                loader.classList.add("is-visible");
                loader.setAttribute("aria-hidden", "false");
            }

            if (submitButton) {
                submitButton.disabled = true;
                submitButton.setAttribute("aria-busy", "true");
            }
        });
    });
}

function initializeCopyButtons() {
    document.querySelectorAll("[data-copy-target]").forEach(button => {
        button.addEventListener("click", async () => {
            const target = document.querySelector(button.dataset.copyTarget);
            const status = button.parentElement?.querySelector("[data-copy-status]");

            if (!target) {
                updateCopyStatus(status, "Contenu introuvable.", true);
                return;
            }

            try {
                await navigator.clipboard.writeText(target.value || target.textContent || "");
                updateCopyStatus(status, "Copié dans le presse-papiers.", false);
            } catch {
                updateCopyStatus(status, "La copie automatique a échoué. Sélectionnez le texte manuellement.", true);
            }
        });
    });
}

function updateCopyStatus(status, message, isError) {
    if (!status) {
        return;
    }

    status.textContent = message;
    status.style.color = isError ? "var(--color-danger)" : "var(--color-success)";

    window.setTimeout(() => {
        status.textContent = "";
    }, 5000);
}
