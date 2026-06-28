
function copyLetter() {

    const text = document.getElementById("letterContent").value;

    navigator.clipboard.writeText(text)
        .then(() => {
            alert("Lettre copiée dans le presse-papier !");
        });

}