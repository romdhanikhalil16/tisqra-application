import datetime
import os
import zipfile
from xml.sax.saxutils import escape

OUT = r"C:/Users/Khalil-ROMDHANI/Desktop/VMS/documents/Rapport_VMS_ZUM-IT.docx"

lines = [
    "Titre: Rapport de conception - Visitor Management System (VMS)",
    "Entreprise d'accueil: ZUM-IT",
    f"Date: {datetime.date.today().isoformat()}",
    "",
    "1. Presentation de l'entreprise d'accueil (ZUM-IT)",
    "ZUM-IT est une entreprise orientee solutions digitales qui developpe des plateformes metier pour optimiser les processus internes et l'experience utilisateur. Dans le cadre de ce stage, l'entreprise a confie le projet VMS (Visitor Management System), une solution de gestion des visites et des invitations de reunion avec controle d'acces via QR code. L'environnement de travail est oriente qualite logicielle, securite, scalabilite et documentation.",
    "",
    "2. Contexte general du projet",
    "Le projet VMS repose sur deux composants principaux: un backend Java Spring Boot (dossier vms) et une application mobile Flutter (dossier vms-fe). Le backend expose des API REST sous /vms, gere la persistance PostgreSQL, les migrations Liquibase et les regles de securite JWT/TOTP. L'application mobile consomme ces API pour les parcours employe et visiteur: creation de reunions, gestion des invitations, generation et scan QR.",
    "",
    "3. Problematique",
    "Avant la solution cible, la gestion des visiteurs et des invitations presentait plusieurs risques: traitement manuel, manque de tracabilite, validation lente a l'entree, et controle d'acces insuffisamment securise. Le besoin metier etait de fiabiliser tout le cycle de vie d'une visite: invitation, confirmation, acces, et audit, avec une experience fluide pour les equipes et les visiteurs.",
    "",
    "4. Etude de l'existant (basee sur le code)",
    "Existant backend:",
    "- Architecture en couches claire: web/controllers, service, repository, model/domain, model/dto.",
    "- API principales: meetings, employees, visitors, offices, participants, users, links.",
    "- Securite configuree dans vms-config.xml: endpoints token en Basic Auth, APIs protegees en Bearer JWT, gestion des roles admin/employee/visitor.",
    "- Base de donnees PostgreSQL + migrations versionnees Liquibase.",
    "- Presence d'initialiseurs de donnees (admin/offices).",
    "Existant mobile:",
    "- Application Flutter avec organisation ecrans/controllers/services/models.",
    "- Client HTTP principal via Dio + interceptors d'authentification.",
    "- Modules metier pour reunions, employes, visiteurs, offices, notifications.",
    "- Flux QR present: generation d'un token cote mobile, scan, puis verification avec les endpoints backend.",
    "Constat global:",
    "- Socle technique solide et modulaire.",
    "- Quelques points de coherence a harmoniser (certaines URLs hardcodees dans des services secondaires cote mobile).",
    "",
    "5. Solution proposee",
    "La solution proposee est une plateforme VMS integree backend/mobile couvrant de bout en bout le parcours visiteur:",
    "1) authentification securisee (JWT/TOTP),",
    "2) gestion des reunions et participants,",
    "3) invitation et check-in visiteur,",
    "4) generation et validation de QR code,",
    "5) suivi des statuts metier (brouillon, confirme, en cours, etc.).",
    "Cette approche ameliore la securite, la tracabilite, et la productivite des equipes d'accueil.",
    "",
    "6. Besoins fonctionnels",
    "- Authentifier les employes et visiteurs selon des parcours adaptes.",
    "- Permettre la creation, consultation, modification et suppression des reunions.",
    "- Gerer les entites metier: employes, visiteurs, bureaux (offices).",
    "- Inviter des visiteurs et associer des participants a une reunion.",
    "- Generer un QR code d'acces pour les participants eligibles.",
    "- Scanner et valider un QR code a l'entree en temps reel.",
    "- Assurer des transitions d'etat controlees des reunions (accept, pass, start, cancel).",
    "- Exposer des APIs exploitables et testables (collection Postman).",
    "",
    "7. Besoins non fonctionnels",
    "- Securite: controle d'acces par role, JWT, TOTP, endpoints publics limites.",
    "- Performance: API paginees, architecture service/repository optimisant les acces donnees.",
    "- Maintenabilite: separation claire des couches et DTOs dedies.",
    "- Evolutivite: migrations Liquibase versionnees et architecture modulaire.",
    "- Fiabilite: gestion d'exceptions centralisee et validation des entrees.",
    "- Portabilite: application mobile Flutter multi-plateforme.",
    "- Exploitabilite: guide de demarrage et documentation technique du systeme.",
    "",
    "8. Use case global",
    "Acteurs principaux: Employe, Visiteur, Administrateur, Agent d'accueil/scanner.",
    "Scenario nominal:",
    "1) L'employe se connecte et cree une reunion en ajoutant visiteurs/participants.",
    "2) Le systeme enregistre la reunion et prepare les informations d'acces.",
    "3) Le participant recupere son QR code (secret + token signe).",
    "4) A l'arrivee, l'agent scanne le QR via l'application mobile.",
    "5) L'application verifie le token et interroge le backend (resolve/secret).",
    "6) Le backend valide le droit d'acces selon participant, statut et temporalite.",
    "7) Le resultat (acces autorise/refuse) est affiche et trace.",
    "",
    "9. Technologies utilisees",
    "Backend:",
    "- Java 17, Spring Boot 3, Spring Security, Spring Data JPA, Querydsl.",
    "- PostgreSQL, Liquibase.",
    "- Maven, Docker/Docker Compose.",
    "Mobile:",
    "- Flutter (Dart), Dio, Provider.",
    "- Firebase (notifications/config mobile).",
    "Outils qualite/documentation:",
    "- Postman, documentation Markdown (vms.md, vms-fe.md, workflow.md, setup-guide.md).",
    "",
    "10. Architecture logique",
    "Vue logique backend (3 couches):",
    "- Presentation: controllers REST (web).",
    "- Metier: services (regles, transitions, orchestration).",
    "- Donnees: repositories + entites (JPA).",
    "Vue logique mobile:",
    "- Presentation: ecrans Flutter.",
    "- Coordination: controllers.",
    "- Acces distant: services API (Dio) + interceptors auth.",
    "- Modele: classes metier/DTO mobile.",
    "Flux transversal:",
    "- Mobile <-> API REST backend <-> Base PostgreSQL.",
    "",
    "11. Architecture physique",
    "- Client mobile Flutter installe sur smartphone/tablette.",
    "- Serveur applicatif backend Spring Boot expose sur /vms.",
    "- Serveur base de donnees PostgreSQL separe.",
    "- Environnement conteneurise possible via Docker Compose (backend + db).",
    "- Echanges reseau HTTPS recommandes en production.",
    "",
    "12. Conclusion",
    "Le projet VMS repond a une problematique metier concrete avec une solution moderne, securisee et evolutive. L'analyse du code montre une base technique robuste et un flux fonctionnel coherent entre backend et mobile. Cette experience est tres positive: elle combine exigences reelles, bonnes pratiques d'ingenierie et impact operationnel direct.",
]


def make_doc_xml(paragraphs):
    body = "".join(
        f"<w:p><w:r><w:t>{escape(line)}</w:t></w:r></w:p>" for line in paragraphs
    )
    return (
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        "<w:document xmlns:wpc=\"http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas\" "
        "xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" "
        "xmlns:o=\"urn:schemas-microsoft-com:office:office\" "
        "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" "
        "xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\" "
        "xmlns:v=\"urn:schemas-microsoft-com:vml\" "
        "xmlns:wp14=\"http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing\" "
        "xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" "
        "xmlns:w10=\"urn:schemas-microsoft-com:office:word\" "
        "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
        "xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\" "
        "xmlns:wpg=\"http://schemas.microsoft.com/office/word/2010/wordprocessingGroup\" "
        "xmlns:wpi=\"http://schemas.microsoft.com/office/word/2010/wordprocessingInk\" "
        "xmlns:wne=\"http://schemas.microsoft.com/office/word/2006/wordml\" "
        "xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\" "
        "mc:Ignorable=\"w14 wp14\">"
        f"<w:body>{body}"
        "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
        "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" "
        "w:header=\"708\" w:footer=\"708\" w:gutter=\"0\"/></w:sectPr>"
        "</w:body></w:document>"
    )


CONTENT_TYPES = (
    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
    "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
    "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
    "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
    "<Override PartName=\"/word/document.xml\" "
    "ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>"
    "<Override PartName=\"/docProps/core.xml\" "
    "ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\"/>"
    "<Override PartName=\"/docProps/app.xml\" "
    "ContentType=\"application/vnd.openxmlformats-officedocument.extended-properties+xml\"/>"
    "</Types>"
)

RELS = (
    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
    "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
    "<Relationship Id=\"rId1\" "
    "Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" "
    "Target=\"word/document.xml\"/>"
    "<Relationship Id=\"rId2\" "
    "Type=\"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties\" "
    "Target=\"docProps/core.xml\"/>"
    "<Relationship Id=\"rId3\" "
    "Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties\" "
    "Target=\"docProps/app.xml\"/>"
    "</Relationships>"
)

WORD_RELS = (
    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
    "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"/>"
)

APP_XML = (
    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
    "<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\" "
    "xmlns:vt=\"http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes\">"
    "<Application>Microsoft Office Word</Application></Properties>"
)

created = f"{datetime.datetime.utcnow().isoformat()}Z"
CORE_XML = (
    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
    "<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" "
    "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
    "xmlns:dcterms=\"http://purl.org/dc/terms/\" "
    "xmlns:dcmitype=\"http://purl.org/dc/dcmitype/\" "
    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
    "<dc:title>Rapport VMS ZUM-IT</dc:title>"
    "<dc:creator>Khalil Romdhani</dc:creator>"
    "<cp:lastModifiedBy>Codex</cp:lastModifiedBy>"
    f"<dcterms:created xsi:type=\"dcterms:W3CDTF\">{created}</dcterms:created>"
    "</cp:coreProperties>"
)

os.makedirs(os.path.dirname(OUT), exist_ok=True)
with zipfile.ZipFile(OUT, "w", zipfile.ZIP_DEFLATED) as docx:
    docx.writestr("[Content_Types].xml", CONTENT_TYPES)
    docx.writestr("_rels/.rels", RELS)
    docx.writestr("word/document.xml", make_doc_xml(lines))
    docx.writestr("word/_rels/document.xml.rels", WORD_RELS)
    docx.writestr("docProps/app.xml", APP_XML)
    docx.writestr("docProps/core.xml", CORE_XML)

print(OUT)
