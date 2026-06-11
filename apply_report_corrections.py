# -*- coding: utf-8 -*-
"""Corrections ciblées du rapport VMS — acteurs, check-in, flux employé."""

from __future__ import annotations

import re
import shutil
from pathlib import Path

from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.shared import Pt, RGBColor
from docx.enum.table import WD_TABLE_ALIGNMENT

ROOT = Path(r"c:\Users\Khalil-ROMDHANI\Desktop\VMS\reportss")
# Source : version complète si disponible, sinon fichier principal
PRIMARY = ROOT / "Rapport Khalil Romdhani.docx"
COMPLETE = ROOT / "Rapport Khalil Romdhani - COMPLETE.docx"
BACKUP = ROOT / "Rapport Khalil Romdhani.pre-corrections.docx"

FUTURE_AGENT_NOTE = (
    "Dans le cadre de l'évolution prévue du système, un troisième acteur — l'Agent de Sécurité — "
    "sera intégré dans les prochaines versions de l'application. Son rôle consistera à superviser "
    "les accès physiques et à interagir avec le dispositif de scan matériel en cours de déploiement."
)

CHECKIN_CURRENT = (
    "Dans l'état actuel du système, le check-in est effectué manuellement par le visiteur "
    "directement via l'interface web de l'application. Cette procédure est utilisée "
    "quotidiennement le matin lors de l'arrivée, dans le cadre de la phase de test en cours. "
    "Aucun dispositif physique de scan n'est intégré à ce stade."
)

CHECKIN_FUTURE = (
    "L'une des évolutions majeures prévues pour les prochaines versions du système est "
    "l'intégration d'un dispositif physique de scan (lecteur de badge, QR code, ou équivalent). "
    "Ce dispositif permettra d'automatiser le processus de check-in sans intervention manuelle "
    "de la part du visiteur, renforçant ainsi la rapidité et la fiabilité du pointage."
)

ACTOR_INTRO = (
    "Le système Zentry (VMS) distingue, dans sa version actuellement testée en interne, "
    "deux acteurs principaux interagissant avec la plateforme. Le tableau suivant synthétise "
    "leurs rôles respectifs au regard du processus de check-in matinal et de l'administration "
    "du système."
)

VISITEUR_DESC = (
    "Acteur principal de l'application. Effectue le check-in manuellement via l'interface web "
    "de l'application, notamment le matin lors de son arrivée. Aucune authentification requise "
    "pour cette action."
)

ADMIN_DESC = (
    "Responsable de la gestion complète du système : gestion des utilisateurs, supervision des "
    "check-ins, configuration de l'application et accès aux tableaux de bord et rapports."
)


def style_run(run, size=12, bold=False, italic=False):
    run.font.name = "Times New Roman"
    run.font.size = Pt(size)
    run.bold = bold
    run.italic = italic
    run.font.color.rgb = RGBColor(0, 0, 0)


def add_para_after(doc: Document, index: int, text: str, italic=False) -> int:
    """Insère un paragraphe après l'index donné via add_paragraph puis réorganisation."""
    p = doc.paragraphs[index]._element
    new_p = doc.add_paragraph()
    new_p.text = text
    new_p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    for r in new_p.runs:
        style_run(r, italic=italic)
    p.addnext(new_p._element)
    return index + 1


def set_paragraph_text(paragraph, text: str, italic=False, bold=False):
    paragraph.clear()
    run = paragraph.add_run(text)
    style_run(run, italic=italic, bold=bold)
    paragraph.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY


def replace_in_paragraph(paragraph, replacements: list[tuple[str, str]]):
    text = paragraph.text
    new = text
    for old, new_val in replacements:
        new = new.replace(old, new_val)
    if new != text:
        set_paragraph_text(paragraph, new)


def map_actor_column(value: str) -> str:
    v = value.strip()
    if not v:
        return v
    low = v.lower()
    if "visiteur" in low and "admin" not in low:
        return "Visiteur"
    if "admin" in low:
        return "Administrateur"
    # employé, agent, participant, hôte, invité, public, authentifié → Administrateur ou Visiteur
    if any(x in low for x in ("visiteur", "public", "check-in", "checkin")):
        return "Visiteur"
    return "Administrateur"


def fix_actor_table(table) -> bool:
    hdr = [c.text.strip().lower() for c in table.rows[0].cells]
    if "acteur" not in hdr[0] and not any("acteur" in h for h in hdr):
        return False
    act_col = next(i for i, h in enumerate(hdr) if "acteur" in h)
    changed = False
    for row in table.rows[1:]:
        cell = row.cells[act_col]
        old = cell.text.strip()
        if old in ("Visiteur", "Administrateur"):
            continue
        if "Synthèse des acteurs" in str(table):
            continue
        new = map_actor_column(old)
        if old != new and old:
            cell.text = new
            changed = True
    return changed


def replace_acteurs_synthese_table(table):
    """Tableau 2.1 — exactement deux acteurs."""
    if len(table.rows) < 2:
        return
    hdr = [c.text for c in table.rows[0].cells]
    if hdr[0].strip() != "Acteur":
        return
    # Garder en-tête, remplacer lignes de données
    while len(table.rows) > 1:
        table._tbl.remove(table.rows[1]._tr)
    for actor, auth, scope in [
        ("Visiteur", "Aucune (check-in web public)", "Check-in matinal manuel via interface web"),
        (
            "Administrateur",
            "JWT / authentification back-office",
            "Gestion utilisateurs, supervision check-ins, configuration, rapports",
        ),
    ]:
        row = table.add_row().cells
        row[0].text = actor
        row[1].text = auth
        row[2].text = scope


def rewrite_section_211(doc: Document):
    """Réécrit le bloc 2.1.1 parties prenantes."""
    start = None
    end = None
    for i, p in enumerate(doc.paragraphs):
        t = p.text.strip()
        if t.startswith("2.1.1") and "parties prenantes" in t:
            start = i
        elif start is not None and t.startswith("2.1.2"):
            end = i
            break
    if start is None:
        return

    set_paragraph_text(doc.paragraphs[start], "2.1.1 Identification des parties prenantes", bold=True)

    body_start = start + 1
    if end is None:
        end = body_start + 6

    new_bodies = [
        ACTOR_INTRO,
        f"Visiteur — {VISITEUR_DESC}",
        f"Administrateur — {ADMIN_DESC}",
        FUTURE_AGENT_NOTE,
    ]

    idx = body_start
    for j, txt in enumerate(new_bodies):
        if idx < end and idx < len(doc.paragraphs):
            set_paragraph_text(doc.paragraphs[idx], txt, italic=(j == len(new_bodies) - 1))
            idx += 1

    # Vider paragraphes restants avant 2.1.2
    while idx < end and idx < len(doc.paragraphs):
        if doc.paragraphs[idx].text.strip().startswith("Tableau 2.1"):
            idx += 1
            continue
        set_paragraph_text(doc.paragraphs[idx], "")
        idx += 1


def rewrite_checkin_use_case(doc: Document):
    """Met à jour la fiche BF-12 et les tables check-in."""
    bf12_idx = None
    for i, p in enumerate(doc.paragraphs):
        if "BF-12" in p.text and "Check-in" in p.text:
            bf12_idx = i
            break

    for table in doc.tables:
        if not table.rows or table.rows[0].cells[0].text.strip() != "Élément":
            continue
        labels = [row.cells[0].text.strip() for row in table.rows]
        if "Acteur principal" not in labels:
            continue
        # BF-12 ou mention check-in dans scénario
        scenario_text = " ".join(
            row.cells[1].text for row in table.rows if len(row.cells) > 1
        ).lower()
        if "check-in" not in scenario_text and "checkin" not in scenario_text:
            if bf12_idx is None:
                continue
        for row in table.rows:
            label = row.cells[0].text.strip()
            if label == "Acteur principal":
                row.cells[1].text = "Visiteur"
            elif label == "Préconditions":
                row.cells[1].text = (
                    "Interface web de check-in accessible ; identifiant employé à vérifier saisi"
                )
            elif label == "Postconditions":
                row.cells[1].text = (
                    "Check-in enregistré avec horodatage si employé trouvé ; sinon message d'erreur"
                )
            elif label == "Scénario principal":
                row.cells[1].text = (
                    "1. Le visiteur accède à l'interface de check-in.\n"
                    "2. Il saisit son identifiant et les informations demandées.\n"
                    "3. Le système recherche l'employé correspondant dans la base de données.\n"
                    "4. Si l'employé est trouvé : le check-in est validé et enregistré avec horodatage.\n"
                    "5. Si l'employé n'est pas trouvé : le check-in est refusé et un message d'erreur s'affiche."
                )
            elif label == "Scénarios alternatifs":
                row.cells[1].text = (
                    "A1 — Employé introuvable : affichage message d'erreur, aucun enregistrement.\n"
                    "A2 — Données invalides : validation formulaire côté client."
                )
        break


def insert_checkin_flow_table(doc: Document):
    if any("4b" in p.text and "Employé non trouvé" in p.text for p in doc.paragraphs):
        if any(CHECKIN_CURRENT[:30] in p.text for p in doc.paragraphs):
            return

    anchor_idx = None
    for i, p in enumerate(doc.paragraphs):
        if "2.3.5" in p.text and "BF-12" in p.text:
            anchor_idx = i
            break
    if anchor_idx is None:
        for i, p in enumerate(doc.paragraphs):
            if "Séquence 4 — Check-in" in p.text:
                anchor_idx = i
                break
    if anchor_idx is None:
        return

    insert_after = doc.paragraphs[anchor_idx]._element
    for txt in [CHECKIN_CURRENT, CHECKIN_FUTURE]:
        np = doc.add_paragraph(txt)
        np.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
        for r in np.runs:
            style_run(r, italic=True)
        insert_after.addnext(np._element)
        insert_after = np._element

    cap = doc.add_paragraph("Tableau 2.8 — Processus de check-in visiteur (vérification employé)")
    cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
    for r in cap.runs:
        style_run(r, bold=True, size=11)
    insert_after.addnext(cap._element)
    insert_after = cap._element

    table = doc.add_table(rows=6, cols=3)
    try:
        table.style = "Table Grid"
    except KeyError:
        pass
    headers = ["Étape", "Action", "Résultat attendu"]
    rows = [
        ["1", "Le visiteur accède à l'interface de check-in", "Formulaire de check-in affiché"],
        ["2", "Le visiteur saisit son identifiant", "Champ rempli et prêt à être soumis"],
        ["3", "Le système recherche l'employé dans la base de données", "Requête de vérification lancée"],
        ["4a", "Employé trouvé", "Check-in validé et enregistré avec horodatage"],
        ["4b", "Employé non trouvé", "Check-in refusé — message d'erreur affiché au visiteur"],
    ]
    for j, h in enumerate(headers):
        table.rows[0].cells[j].text = h
    for ri, row in enumerate(rows, 1):
        for ci, val in enumerate(row):
            table.rows[ri].cells[ci].text = val
    insert_after.addnext(table._tbl)


def global_consistency_pass(doc: Document):
    actor_list_patterns = [
        (r"quatre catégories d'acteurs", "deux acteurs principaux"),
        (r"L'employé standard dispose", ""),
        (r"L'administrateur est un employé", "L'Administrateur dispose"),
        (r"L'agent de sécurité est modélisé", ""),
        (r"Acteurs participant et agent sécurité", "Cas d'utilisation couverts par le Visiteur et l'Administrateur"),
        (r"employés scanner", "administration (évolution : agent de sécurité avec dispositif de scan)"),
        (r"Les acteurs Administrateur et Agent sécurité", "L'Administrateur"),
        (r"Agent sécurité \(employé scanner\)", "Administrateur (évolution future : Agent de Sécurité)"),
        (r"Agent sécurité", "Administrateur"),
        (r"Employé / Visiteur", "Visiteur / Administrateur"),
        (r"En tant qu'employé", "En tant qu'Administrateur"),
        (r"Login employé", "Connexion administrateur"),
        (r"Connexion employé", "Connexion administrateur"),
        (r"authentification employé", "authentification administrateur"),
        (r"BF-01 — Connexion employé", "BF-01 — Connexion administrateur"),
        (r"Acteur principal\", \"Employé", 'Acteur principal", "Administrateur'),
    ]

    qr_current_phrases = [
        (
            "mécanisme instantané de génération et de validation de QR code",
            "mécanisme de génération et de validation de QR code (prévu avec le dispositif de scan physique)",
        ),
        (
            "prototype scan QR testé avec QR imprimé",
            "prototype de scan QR envisagé pour la prochaine version avec dispositif physique",
        ),
        (
            "Séquence 3 — Validation QR : Scan",
            "Séquence 3 — Validation QR (évolution future) : Scan via dispositif matériel",
        ),
        (
            "Scanner sécurité : Vue caméra plein écran",
            "Scanner (évolution future) : dispositif physique ou caméra — non déployé en phase de test actuelle",
        ),
        (
            "MeetingScannerController",
            "composant de scan (prévu pour intégration dispositif physique — non actif en phase de test manuelle)",
        ),
    ]

    sequence_checkin = (
        "Séquence 4 — Check-in visiteur : le visiteur accède à l'interface web → saisit son "
        "identifiant → le système recherche l'employé en base → branche 4a (employé trouvé : "
        "check-in enregistré avec horodatage) ou branche 4b (employé non trouvé : refus et message d'erreur)."
    )

    for p in doc.paragraphs:
        t = p.text
        if not t.strip():
            continue

        new_t = t
        for old, new in actor_list_patterns:
            if old and old in new_t:
                new_t = new_t.replace(old, new)
            elif old in new_t and new == "":
                new_t = ""  # suppress obsolete paragraph

        for old, new in qr_current_phrases:
            if old in new_t:
                new_t = new_t.replace(old, new)

        if "Séquence 4 — Check-in visiteur : POST /visitors/checkin" in new_t:
            new_t = sequence_checkin

        if "POST /visitors/checkin avec VisitorDto" in new_t and "employé" not in new_t.lower():
            new_t = (
                "1. Le visiteur accède au formulaire web de check-in.\n"
                "2. Il saisit son identifiant.\n"
                "3. Le système recherche l'employé en base.\n"
                "4a. Employé trouvé → check-in validé avec horodatage.\n"
                "4b. Employé non trouvé → refus et message d'erreur."
            )

        if "2.3.4" in t and "BF-11" in t:
            new_t = "2.3.4 BF-11 — Validation QR à l'entrée (évolution future — dispositif de scan)"

        if new_t != t:
            if new_t.strip():
                set_paragraph_text(p, new_t, bold=("2.3.4 BF-11" in new_t))
            else:
                set_paragraph_text(p, "")

    # Table BF acteur column
    for table in doc.tables:
        hdr = [c.text.strip() for c in table.rows[0].cells]
        if len(hdr) >= 3 and hdr[2] == "Acteur":
            for row in table.rows[1:]:
                val = row.cells[2].text
                if "BF-11" in row.cells[0].text or "Scanner" in row.cells[1].text:
                    row.cells[2].text = "Administrateur (évolution : Agent de Sécurité + dispositif scan)"
                else:
                    row.cells[2].text = map_actor_column(val)
        if len(hdr) >= 4 and hdr[3] == "Autorisation":
            for row in table.rows[1:]:
                auth = row.cells[3].text
                if "Employé" in auth or "Admin" in auth:
                    row.cells[3].text = re.sub(
                        r"Employé\s*/?\s*Visiteur\s*/?\s*Admin?",
                        "Visiteur / Administrateur",
                        auth,
                    ).replace("Employé", "Administrateur").replace("Admin", "Administrateur")
        if hdr[0] == "Acteur" and "Authentification" in "".join(hdr):
            replace_acteurs_synthese_table(table)

        # Fiches cas d'utilisation (Élément / Description)
        if table.rows and table.rows[0].cells[0].text.strip() == "Élément":
            for row in table.rows:
                if row.cells[0].text.strip() == "Acteur principal":
                    val = row.cells[1].text
                    if any(x in val for x in ("Agent", "Employé", "employé", "scanner")):
                        ctx = " ".join(c.text for r in table.rows for c in r.cells)
                        if "BF-11" in ctx or "Validation QR" in ctx or "Scanner" in ctx:
                            row.cells[1].text = (
                                "Administrateur (fonction prévue pour l'Agent de Sécurité "
                                "avec dispositif de scan — non actif en phase de test manuelle)"
                            )
                        else:
                            row.cells[1].text = map_actor_column(val)

    # Perspectives : ensure CHECKIN_FUTURE present
    has_future = any(CHECKIN_FUTURE[:40] in p.text for p in doc.paragraphs)
    if not has_future:
        for i, p in enumerate(doc.paragraphs):
            if "Perspectives d'évolution" in p.text or "Perspectives d'évolution" in p.text:
                add_para_after(doc, i, CHECKIN_FUTURE, italic=True)
                add_para_after(doc, i + 1, FUTURE_AGENT_NOTE, italic=True)
                break


def add_short_report_corrections(doc: Document):
    """Corrections pour rapport court (sans ch. 2 étendu)."""
    anchor = None
    for i, p in enumerate(doc.paragraphs):
        if "ergonomie" in p.text and "caractéristiques" in p.text:
            anchor = i
            break
    if anchor is None:
        return

    insert_el = doc.paragraphs[anchor]._element
    blocks = [
        ("2.1.1 Identification des parties prenantes", False, True),
        (ACTOR_INTRO, False, False),
        (f"Visiteur — {VISITEUR_DESC}", False, False),
        (f"Administrateur — {ADMIN_DESC}", False, False),
        (FUTURE_AGENT_NOTE, True, False),
        ("2.1.2 Processus de check-in (état actuel et évolution)", False, True),
        (CHECKIN_CURRENT, True, False),
        (CHECKIN_FUTURE, True, False),
    ]
    for txt, italic, bold in blocks:
        np = doc.add_paragraph()
        run = np.add_run(txt)
        style_run(run, bold=bold, italic=italic)
        np.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
        insert_el.addnext(np._element)
        insert_el = np._element

    table = doc.add_table(rows=6, cols=3)
    hdr = table.rows[0].cells
    for j, h in enumerate(["Étape", "Action", "Résultat attendu"]):
        hdr[j].text = h
    data = [
        ["1", "Le visiteur accède à l'interface de check-in", "Formulaire de check-in affiché"],
        ["2", "Le visiteur saisit son identifiant", "Champ rempli et prêt à être soumis"],
        ["3", "Le système recherche l'employé dans la base de données", "Requête de vérification lancée"],
        ["4a", "Employé trouvé", "Check-in validé et enregistré avec horodatage"],
        ["4b", "Employé non trouvé", "Check-in refusé — message d'erreur affiché au visiteur"],
    ]
    for ri, row in enumerate(data, 1):
        for ci, val in enumerate(row):
            table.rows[ri].cells[ci].text = val
    insert_el.addnext(table._tbl)

    # Note after actor section heading if exists
    for p in doc.paragraphs:
        if "Acteurs et besoins identifiés" in p.text:
            continue
        replace_in_paragraph(
            p,
            [
                (
                    "mécanisme instantané de génération et de validation de QR code",
                    "mécanisme de validation par QR code prévu avec le futur dispositif de scan",
                ),
            ],
        )


def main():
    src = COMPLETE if COMPLETE.exists() else PRIMARY
    if not PRIMARY.exists() and not COMPLETE.exists():
        raise FileNotFoundError("Aucun rapport trouvé dans reportss/")

    if not BACKUP.exists():
        shutil.copy2(PRIMARY, BACKUP)

    doc = Document(str(src))
    is_short = not any("2.1.1" in p.text for p in doc.paragraphs)

    if is_short:
        add_short_report_corrections(doc)
        global_consistency_pass(doc)
    else:
        rewrite_section_211(doc)
        for table in doc.tables:
            if table.rows and table.rows[0].cells[0].text.strip() == "Acteur":
                if "Authentification" in table.rows[0].cells[1].text:
                    replace_acteurs_synthese_table(table)
        rewrite_checkin_use_case(doc)
        insert_checkin_flow_table(doc)
        global_consistency_pass(doc)
        # Note acteurs après tableau 2.1
        for i, p in enumerate(doc.paragraphs):
            if p.text.strip().startswith("Tableau 2.1"):
                if i + 1 < len(doc.paragraphs) and FUTURE_AGENT_NOTE[:30] not in doc.paragraphs[i + 1].text:
                    add_para_after(doc, i, FUTURE_AGENT_NOTE, italic=True)
                break

    out = PRIMARY
    try:
        doc.save(str(out))
    except PermissionError:
        out = ROOT / "Rapport Khalil Romdhani - CORRECTED.docx"
        doc.save(str(out))
        print(f"Fichier original verrouillé. Sauvegardé sous : {out}")
        return
    print(f"Corrections appliquées → {out}")
    print(f"Source : {src.name}")


if __name__ == "__main__":
    main()
