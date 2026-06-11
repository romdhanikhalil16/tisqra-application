import os
import shutil
from datetime import datetime

ROOT = r"C:/Users/Khalil-ROMDHANI/Desktop/VMS"
DOCS_SRC = os.path.join(ROOT, "documents")
OUT_DIR = os.path.join(ROOT, "document")
DIAG_DIR = os.path.join(OUT_DIR, "generated-diagrams")

os.makedirs(OUT_DIR, exist_ok=True)
os.makedirs(DIAG_DIR, exist_ok=True)


def backup_if_exists(path: str):
    if os.path.exists(path):
        stamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        backup = f"{path}.bak_{stamp}"
        shutil.copy2(path, backup)
        return backup
    return None


# 1) Update internship journal with sprint-synced content
journal_path = os.path.join(DOCS_SRC, "journale de stage")
journal_backup = backup_if_exists(journal_path)

journal_text = """Journal de stage synchronise par sprints
Stagiaire : Khalil Romdhani
Periode : Decembre 2025 - Juin 2026
Projet : Visitor Management System (VMS)

Sprint 1 - Authentification et gestion des roles
- Objectifs : Mettre en place l'authentification employee/visitor et le controle d'acces par roles.
- Taches realisees : Analyse des endpoints /employees/token et /visitors/token, verification des regles de securite vms-config.xml, validation du flux Bearer JWT cote mobile.
- Technologies utilisees : Spring Security, JWT, TOTP, Flutter Dio interceptor, Provider.
- Defis rencontres : Comprendre les regles d'autorisation XML et les differences entre routes publiques/protegees.
- Resume de progression : Authentification maitrisee de bout en bout avec une vision claire des droits d'acces.

Sprint 2 - Gestion des reunions
- Objectifs : Couvrir le cycle de vie des reunions (CRUD + transitions d'etat).
- Taches realisees : Cartographie des endpoints meetings, etude des DTO MeetingDto/MeetingResolveDto, verification des actions accept/pass/start/cancel.
- Technologies utilisees : Spring Boot, JPA, Querydsl, Flutter services/controllers.
- Defis rencontres : Coherence des transitions d'etat et cas limites temporels.
- Resume de progression : Fonctionnement metier des reunions clarifie et documente.

Sprint 3 - Flux QR (generation et validation)
- Objectifs : Securiser le controle d'acces via QR code.
- Taches realisees : Analyse du flux get secret participant -> generation token QR -> scan -> resolve meeting, verification des ecrans scanner/resultat.
- Technologies utilisees : JWT helper mobile, endpoint /participants/{meetingId}/secret, endpoint /meetings/{id}/resolve.
- Defis rencontres : Synchronisation backend/mobile sur la validation du participant et du statut meeting.
- Resume de progression : Flux QR trace et valide, avec un schema fonctionnel stable.

Sprint 4 - Gestion visiteurs et check-in
- Objectifs : Digitaliser l'enregistrement visiteur et fluidifier l'accueil.
- Taches realisees : Revue endpoints visitors (CRUD, redeem, check-in, token), verification du parcours visitorpart.
- Technologies utilisees : Spring Boot REST, Flutter visitor module, Dio, models JSON.
- Defis rencontres : Gestion des scenarios visiteurs (nouveau visiteur, redeem, connexion TOTP).
- Resume de progression : Parcours visiteur rendu coherent et exploitable en environnement reel.

Sprint 5 - Administration et gouvernance
- Objectifs : Consolider la gestion administrative (offices, supervision des donnees).
- Taches realisees : Revue des endpoints offices et users, verification des regles d'autorisation admin/employee, ajout de cardinalites dans les diagrammes UML.
- Technologies utilisees : Spring Boot, PostgreSQL, Liquibase, Mermaid/PlantUML.
- Defis rencontres : Harmoniser la documentation et la conception UML avec le code existant.
- Resume de progression : Documentation technique et diagrams alignes avec l'architecture applicative.

Sprint 6 - Documentation finale et industrialisation
- Objectifs : Produire les livrables de rapport et de maintenance.
- Taches realisees : Generation de la collection Postman, redaction vms.md / vms-fe.md / workflow.md / setup-guide.md, preparation du rapport latex.
- Technologies utilisees : Markdown, LaTeX, Postman JSON, scripts Python.
- Defis rencontres : Garder une coherence globale entre code, API, UML et narration de stage.
- Resume de progression : Livrables finalises avec une presentation professionnelle et reutilisable.

Bilan general
Cette experience de stage est tres positive. Elle m'a permis de renforcer mes competences full-stack, de mieux comprendre la conception d'une architecture orientee securite, et de produire des livrables professionnels utiles a l'equipe.
"""

with open(journal_path, "w", encoding="utf-8") as f:
    f.write(journal_text)


uml_files = {
    "class_authentication.uml": """@startuml
title Sprint 1 - Class Diagram - Authentification
class AuthController {
  -jwtService: JwtService
  -employeeService: EmployeeService
  +loginEmployee(email, password): String
  +loginVisitor(email, totpCode): String
}
class EmployeeService {
  -employeeRepository: EmployeeRepository
  -passwordEncoder: PasswordEncoder
  +authenticate(email, password): Employee
  +loadUserByUsername(email): Employee
}
class JwtService {
  -privateKey: String
  -publicKey: String
  -ttlSeconds: long
  +generateToken(subject, scope): String
  +validateToken(token): boolean
}
class Employee {
  -email: String
  -passwordHash: String
  -role: String
  +checkPassword(raw): boolean
  +toScope(): String
}
class TokenHolder {
  -accessToken: String
  -refreshAt: String
  -userEmail: String
  +getToken(): String
  +clearToken(): void
}
class AuthServiceMobile {
  -dioClient: DioClient
  -tokenHolder: TokenHolder
  +loginEmployee(email, password): String
  +saveToken(jwt): void
}
AuthController "1" --> "1" EmployeeService
AuthController "1" --> "1" JwtService
EmployeeService "1" --> "0..*" Employee
EmployeeService "1" --> "1" EmployeeRepository
AuthServiceMobile "1" --> "1" TokenHolder
AuthServiceMobile "1" --> "1" AuthController
@enduml
""",
    "class_meetings.uml": """@startuml
title Sprint 2 - Class Diagram - Meetings
class MeetingController {
  -meetingService: MeetingService
  -securityContext: SecurityContext
  +createMeeting(dto): MeetingDto
  +updateMeeting(id, dto): MeetingDto
}
class MeetingService {
  -meetingRepository: MeetingRepository
  -officeRepository: OfficeRepository
  -employeeRepository: EmployeeRepository
  +create(dto, actor): Meeting
  +changeStatus(id, status): void
}
class MeetingRepository {
  -entityManager: EntityManager
  -tableName: String
  +save(meeting): Meeting
  +findById(id): Meeting
}
class Meeting {
  -id: Long
  -topic: String
  -status: String
  +isEditable(): boolean
  +setStatus(newStatus): void
}
class MeetingServiceMobile {
  -dioClient: DioClient
  -baseUrl: String
  +fetchMeetings(page): List
  +createMeeting(data): MeetingModel
}
MeetingController "1" --> "1" MeetingService
MeetingService "1" --> "1" MeetingRepository
MeetingRepository "1" --> "0..*" Meeting
MeetingServiceMobile "1" --> "1" MeetingController
@enduml
""",
    "class_qr_access.uml": """@startuml
title Sprint 3 - Class Diagram - QR Access
class ParticipantController {
  -participantService: ParticipantService
  -meetingService: MeetingService
  +getSecret(meetingId): UserSecretDto
  +resolveMeeting(meetingId, participant): MeetingResolveDto
}
class ParticipantService {
  -meetingRepository: MeetingRepository
  -employeeRepository: EmployeeRepository
  -visitorRepository: VisitorRepository
  +fetchParticipantSecret(meetingId, user): String
  +isParticipantAllowed(meetingId, email): boolean
}
class UserSecretDto {
  -displayName: String
  -secret: String
  -meetingId: Long
  +maskSecret(): String
  +toJson(): Map
}
class MeetingScannerControllerMobile {
  -meetingServiceMobile: MeetingServiceMobile
  -jwtHelper: JwtHelper
  +scanAndDecode(raw): Map
  +verifyAccess(meetingId, participant): boolean
}
ParticipantController "1" --> "1" ParticipantService
ParticipantService "1" --> "0..*" UserSecretDto
MeetingScannerControllerMobile "1" --> "1" ParticipantController
@enduml
""",
    "class_visitors.uml": """@startuml
title Sprint 4 - Class Diagram - Visitors
class VisitorController {
  -visitorService: VisitorService
  -tokenService: TokenService
  +createVisitor(dto): VisitorDto
  +checkIn(dto): void
}
class VisitorService {
  -visitorRepository: VisitorRepository
  -meetingRepository: MeetingRepository
  -tokenRepository: TokenRepository
  +redeem(code, visitor): FullVisitorDto
  +checkIn(dto): boolean
}
class Visitor {
  -email: String
  -firstname: String
  -lastname: String
  -company: String
  +fullName(): String
  +updateProfile(company, phone): void
}
class VisitorServiceMobile {
  -dioClient: DioClient
  -baseUrl: String
  -tokenHolder: TokenHolder
  +checkIn(data): boolean
  +redeemInvite(code, visitor): Map
}
VisitorController "1" --> "1" VisitorService
VisitorService "1" --> "0..*" Visitor
VisitorServiceMobile "1" --> "1" VisitorController
@enduml
""",
    "class_administration.uml": """@startuml
title Sprint 5 - Class Diagram - Administration
class OfficeController {
  -officeService: OfficeService
  -employeeService: EmployeeService
  +createOffice(dto): OfficeDto
  +updateOffice(id, dto): OfficeDto
}
class OfficeService {
  -officeRepository: OfficeRepository
  -employeeRepository: EmployeeRepository
  -auditService: AuditService
  +assignManager(officeId, managerEmail): void
  +deleteOffice(officeId): void
}
class Office {
  -id: Long
  -name: String
  -city: String
  -country: String
  +changeLocation(city, country): void
  +setManager(email): void
}
class OfficeServiceMobile {
  -dioClient: DioClient
  -baseUrl: String
  +fetchOffices(): List
  +updateOffice(id, payload): OfficeModel
}
OfficeController "1" --> "1" OfficeService
OfficeService "1" --> "0..*" Office
OfficeServiceMobile "1" --> "1" OfficeController
@enduml
""",
    "sequence_authentication.uml": """@startuml
title Sprint 1 - Sequence - Authentification
actor Mobile
participant "API /vms" as API
participant AuthController as C
participant EmployeeService as S
participant EmployeeRepository as R
database PostgreSQL as DB
Mobile -> API : POST /employees/token
API -> C : loginEmployee(email,password)
C -> S : authenticate(email,password)
S -> R : findByEmail(email)
R -> DB : SELECT employee
DB --> R : employee row
R --> S : Employee
S --> C : Employee valide
C --> API : JWT
API --> Mobile : 200 token
@enduml
""",
    "sequence_meeting_creation.uml": """@startuml
title Sprint 2 - Sequence - Creation Reunion
actor Mobile
participant "API /vms" as API
participant MeetingController as C
participant MeetingService as S
participant MeetingRepository as R
database PostgreSQL as DB
Mobile -> API : POST /meetings
API -> C : createMeeting(dto)
C -> S : create(dto, actor)
S -> R : save(meeting)
R -> DB : INSERT meeting
DB --> R : meeting_id
R --> S : Meeting persisted
S --> C : MeetingDto
C --> API : 201 Created
API --> Mobile : Meeting JSON
@enduml
""",
    "sequence_qr_validation.uml": """@startuml
title Sprint 3 - Sequence - Validation QR
actor Scanner
participant "API /vms" as API
participant ParticipantController as C
participant ParticipantService as S
participant MeetingRepository as R
database PostgreSQL as DB
Scanner -> API : GET /meetings/{id}/resolve?participant=x
API -> C : resolveMeeting(id, participant)
C -> S : isParticipantAllowed(id, email)
S -> R : findMeetingWithParticipants(id)
R -> DB : SELECT meeting + participants
DB --> R : data
R --> S : aggregate
S --> C : allowed + secret
C --> API : MeetingResolveDto
API --> Scanner : access decision
@enduml
""",
    "sequence_visitor_checkin.uml": """@startuml
title Sprint 4 - Sequence - Check-in Visiteur
actor Mobile
participant "API /vms" as API
participant VisitorController as C
participant VisitorService as S
participant VisitorRepository as R
database PostgreSQL as DB
Mobile -> API : POST /visitors/checkin
API -> C : checkIn(dto)
C -> S : checkIn(dto)
S -> R : findByEmail(email)
R -> DB : SELECT visitor
DB --> R : visitor row
R --> S : Visitor
S -> DB : UPDATE checkin state
DB --> S : ok
S --> C : true
C --> API : 200 OK
API --> Mobile : success
@enduml
""",
    "sequence_office_update.uml": """@startuml
title Sprint 5 - Sequence - Mise a jour Office
actor AdminMobile
participant "API /vms" as API
participant OfficeController as C
participant OfficeService as S
participant OfficeRepository as R
database PostgreSQL as DB
AdminMobile -> API : PATCH /offices/{id}
API -> C : updateOffice(id,dto)
C -> S : assignManager(id, manager)
S -> R : findById(id)
R -> DB : SELECT office
DB --> R : office row
R --> S : Office
S -> R : save(office)
R -> DB : UPDATE office
DB --> R : ok
R --> S : Office updated
S --> C : OfficeDto
C --> API : 200 updated
API --> AdminMobile : Office JSON
@enduml
""",
    "usecase_authentication.uml": """@startuml
left to right direction
actor Utilisateur
actor Admin
rectangle VMS_Authentification {
  usecase "Se connecter" as UC1
  usecase "Recevoir JWT" as UC2
  usecase "Acceder aux ecrans autorises" as UC3
  usecase "Gerer roles/permissions" as UC4
}
Utilisateur --> UC1
Utilisateur --> UC2
Utilisateur --> UC3
Admin --> UC3
Admin --> UC4
@enduml
""",
    "usecase_meetings.uml": """@startuml
left to right direction
actor Utilisateur
actor Admin
rectangle VMS_Reunions {
  usecase "Creer reunion" as UC1
  usecase "Consulter reunions" as UC2
  usecase "Modifier reunion" as UC3
  usecase "Changer statut" as UC4
  usecase "Supprimer reunion" as UC5
}
Utilisateur --> UC1
Utilisateur --> UC2
Utilisateur --> UC3
Utilisateur --> UC4
Admin --> UC2
Admin --> UC4
Admin --> UC5
@enduml
""",
    "usecase_qr_access.uml": """@startuml
left to right direction
actor Utilisateur
actor Admin
rectangle VMS_QR {
  usecase "Generer QR d'acces" as UC1
  usecase "Scanner QR" as UC2
  usecase "Verifier validite d'acces" as UC3
  usecase "Afficher resultat" as UC4
  usecase "Auditer acces" as UC5
}
Utilisateur --> UC1
Utilisateur --> UC2
Utilisateur --> UC3
Utilisateur --> UC4
Admin --> UC3
Admin --> UC5
@enduml
""",
    "usecase_visitors.uml": """@startuml
left to right direction
actor Utilisateur
actor Admin
rectangle VMS_Visiteurs {
  usecase "Enregistrer visiteur" as UC1
  usecase "Redeem invitation" as UC2
  usecase "Effectuer check-in" as UC3
  usecase "Mettre a jour profil visiteur" as UC4
  usecase "Superviser registre visiteurs" as UC5
}
Utilisateur --> UC1
Utilisateur --> UC2
Utilisateur --> UC3
Utilisateur --> UC4
Admin --> UC4
Admin --> UC5
@enduml
""",
    "usecase_administration.uml": """@startuml
left to right direction
actor Utilisateur
actor Admin
rectangle VMS_Administration {
  usecase "Consulter offices" as UC1
  usecase "Creer/modifier office" as UC2
  usecase "Assigner manager office" as UC3
  usecase "Superviser coherence des donnees" as UC4
}
Utilisateur --> UC1
Admin --> UC1
Admin --> UC2
Admin --> UC3
Admin --> UC4
@enduml
""",
}

for name, content in uml_files.items():
    with open(os.path.join(DIAG_DIR, name), "w", encoding="utf-8") as f:
        f.write(content)


# 3) Generate final latex report under /document/report.tex
report_path = os.path.join(OUT_DIR, "report.tex")
report_backup = backup_if_exists(report_path)
report_tex = r"""\documentclass[12pt,a4paper]{article}
\usepackage[utf8]{inputenc}
\usepackage[T1]{fontenc}
\usepackage[french]{babel}
\usepackage{geometry}
\usepackage{graphicx}
\usepackage{float}
\usepackage{listings}
\usepackage{hyperref}
\geometry{margin=2.5cm}
\title{Rapport de stage -- Visitor Management System (VMS)}
\author{Khalil Romdhani}
\date{\today}
\begin{document}
\maketitle
\tableofcontents
\newpage

\section{Introduction}
Ce rapport presente les travaux realises autour du projet Visitor Management System (VMS), une solution full-stack combinant backend Spring Boot et application mobile Flutter. L'objectif principal est d'automatiser la gestion des visites et des acces reunions via QR code.

\section{Entreprise d'accueil}
L'entreprise d'accueil est ZUM-IT, societe orientee transformation digitale et developpement de solutions metier. Le stage s'inscrit dans une demarche de qualite logicielle, de securite applicative et d'industrialisation documentaire.

\section{Etude de l'existant}
L'etude initiale a montre une base applicative deja structuree en couches (web/service/repository/model) cote backend et une architecture par modules (screens/controllers/services/models) cote mobile. Les flux critiques identifies sont l'authentification, la gestion des reunions, le check-in visiteur et la validation QR.

\section{Besoins fonctionnels et non fonctionnels}
\subsection{Besoins fonctionnels}
\begin{itemize}
  \item Authentifier employes et visiteurs.
  \item Gerer reunions, offices, employes et visiteurs.
  \item Generer/scanner/valider les QR codes d'acces.
  \item Assurer la tracabilite des acces.
\end{itemize}
\subsection{Besoins non fonctionnels}
\begin{itemize}
  \item Securite: JWT, TOTP, controle d'acces par role.
  \item Maintenabilite: architecture modulaire et migrations versionnees.
  \item Performance: endpoints pagines, separation claire des responsabilites.
  \item Portabilite: application mobile Flutter multi-plateforme.
\end{itemize}

\section{Solution proposee}
La solution proposee est une plateforme integree qui relie la creation de reunions, l'invitation de visiteurs, la generation de QR et la verification en temps reel. Le backend centralise les regles metier et la securite, tandis que le mobile assure l'experience operationnelle terrain.

\section{Methodologie}
Le projet a ete structure par sprints avec une approche iterative: analyse du code existant, validation des flux, documentation UML, puis consolidation des livrables (journal, rapport, collection API, guides techniques).

\section{Conception (diagrammes)}
Les diagrammes UML sources sont fournis dans le dossier \texttt{document/generated-diagrams}:
\begin{itemize}
  \item Diagrammes de classes (\texttt{class\_*.uml})
  \item Diagrammes de sequence (\texttt{sequence\_*.uml})
  \item Diagrammes de cas d'utilisation (\texttt{usecase\_*.uml})
\end{itemize}
Exemples de domaines modelises: authentification, gestion des reunions, flux QR, check-in visiteur, administration.

\section{Technologies utilisees}
\subsection{Backend}
Java 17, Spring Boot, Spring Security, Spring Data JPA, Querydsl, PostgreSQL, Liquibase.
\subsection{Frontend mobile}
Flutter (Dart), Dio, Provider, Firebase (configuration/notifications).
\subsection{Outils}
Postman, LaTeX, scripts Python, documentation Markdown.

\section{Architecture}
\subsection{Architecture backend}
Architecture en couches:
\begin{itemize}
  \item Controllers REST (\texttt{web})
  \item Services metier (\texttt{service})
  \item Repositories (\texttt{repository})
  \item Entites/DTO (\texttt{model/domain}, \texttt{model/dto})
\end{itemize}
\subsection{Architecture frontend}
Organisation par fonctionnalites:
\begin{itemize}
  \item \texttt{screens} pour l'UI
  \item \texttt{controllers} pour l'orchestration
  \item \texttt{services} pour les appels API
  \item \texttt{models} pour les objets metier
\end{itemize}
\subsection{Base de donnees}
Base PostgreSQL avec migrations Liquibase. Entites centrales: profiles (employees/visitors), meetings, offices, tokens, confirmations.
\subsection{APIs et services}
Endpoints principaux: \texttt{/meetings}, \texttt{/employees}, \texttt{/visitors}, \texttt{/offices}, \texttt{/participants}, \texttt{/users}, \texttt{/links}.
\subsection{Flux d'authentification}
\begin{itemize}
  \item \texttt{POST /employees/token} (Basic) puis Bearer JWT.
  \item \texttt{POST /visitors/token} (Basic + TOTP) puis Bearer JWT.
\end{itemize}
\subsection{Deploiement}
Structure de deploiement disponible via Docker Compose (backend + PostgreSQL) avec variables d'environnement.

\section{Avancement du projet}
L'avancement est detaille dans le journal de stage mis a jour par sprint. Les livrables principaux produits sont:
\begin{itemize}
  \item Documentation architecture/API/workflow/setup.
  \item Collection Postman complete.
  \item Diagrammes UML sources generes et corriges (cardinalites ajoutees).
  \item Rapport LaTeX final structure.
\end{itemize}

\section{Conclusion}
Le projet VMS est une implementation full-stack coherente et professionnalisee. Le travail realise a permis d'aligner architecture, securite, documentation et modelisation UML. Les bases sont solides pour la maintenance et l'evolution future.

\section*{Bibliographie (placeholder)}
\begin{itemize}
  \item Documentation Spring Boot / Spring Security.
  \item Documentation Flutter / Dart.
  \item Documentation PostgreSQL / Liquibase.
  \item Documentation interne ZUM-IT.
\end{itemize}

\end{document}
"""
with open(report_path, "w", encoding="utf-8") as f:
    f.write(report_tex)


summary_path = os.path.join(OUT_DIR, "generation-summary.md")
summary = f"""# Generation Summary

## Generated files
- `document/report.tex`
- `document/generated-diagrams/class_authentication.uml`
- `document/generated-diagrams/class_meetings.uml`
- `document/generated-diagrams/class_qr_access.uml`
- `document/generated-diagrams/class_visitors.uml`
- `document/generated-diagrams/class_administration.uml`
- `document/generated-diagrams/sequence_authentication.uml`
- `document/generated-diagrams/sequence_meeting_creation.uml`
- `document/generated-diagrams/sequence_qr_validation.uml`
- `document/generated-diagrams/sequence_visitor_checkin.uml`
- `document/generated-diagrams/sequence_office_update.uml`
- `document/generated-diagrams/usecase_authentication.uml`
- `document/generated-diagrams/usecase_meetings.uml`
- `document/generated-diagrams/usecase_qr_access.uml`
- `document/generated-diagrams/usecase_visitors.uml`
- `document/generated-diagrams/usecase_administration.uml`

## Updated files
- `documents/journale de stage` (backup created: `{journal_backup}`)

## Corrected diagrams
- Added explicit multiplicities/cardinalities on class associations.
- Standardized naming across controller/service/repository/mobile service layers.
- Kept actor roles consistent in use cases (`Utilisateur`, `Admin`).
- Normalized sequence flow to explicit `API -> Controller -> Service -> Repository -> DB`.

## Missing elements found
- Temporary file `~$pport_VMS_ZUM-IT.docx` was not available as a readable source at generation time.
- No native Visual Paradigm binary project file (`.vpp`) found; generated UML source provided in PlantUML-compatible `.uml`.

## Assumptions made
- Main authoritative sources: `documents/rapport.tex`, `documents/Rapport_VMS_ZUM-IT.docx`, `documents/journale de stage`, and code-level docs (`vms.md`, `vms-fe.md`, `workflow.md`).
- Sprint breakdown follows the documented functional streams (auth, meetings, QR, visitors, administration, finalization).

## Remaining manual work
- (Optional) Import `.uml` files into Visual Paradigm and adjust layout aesthetics.
- (Optional) Compile `document/report.tex` and include rendered diagram images if required by template.
- Verify if the lock file `~$pport_VMS_ZUM-IT.docx` reappears while Word is open and reconcile any delta content.
"""
with open(summary_path, "w", encoding="utf-8") as f:
    f.write(summary)

print("Generated documentation bundle successfully.")
