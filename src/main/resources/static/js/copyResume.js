function copyResume() {
    const content = document.getElementById("resumeContent");
    navigator.clipboard.writeText(content.value);
}
