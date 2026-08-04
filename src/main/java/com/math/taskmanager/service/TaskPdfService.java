package com.math.taskmanager.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.math.taskmanager.entity.Task;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import java.awt.Color;

import com.math.taskmanager.entity.TaskHistory;

import com.math.taskmanager.entity.Attachment;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskPdfService {
	
	private final TaskHistoryService taskHistoryService;
	
	private final AttachmentService attachmentService;

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generate(Task task) {

        try {

            Document document = new Document();

            document.setMargins(40, 40, 50, 40);

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            PdfWriter.getInstance(document, output);

            document.open();

            Font titleFont =
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);

            Font sectionFont =
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);

            Font labelFont =
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

            Font valueFont =
                    FontFactory.getFont(FontFactory.HELVETICA, 11);

            Paragraph system =
                    new Paragraph("TACITURNO", titleFont);

            system.setAlignment(Paragraph.ALIGN_CENTER);

            document.add(system);

            Paragraph report =
                    new Paragraph("Relatório da Demanda", sectionFont);

            report.setAlignment(Paragraph.ALIGN_CENTER);

            report.setSpacingAfter(20);

            document.add(report);

            document.add(new Paragraph(
                    "Emitido em: "
                            + LocalDateTime.now().format(DATE_TIME),
                    valueFont));

            document.add(new Paragraph(" "));

            addSummary(document, task);

            addTitle(document, task);

            addDescription(document, task);

            addHistory(document, task);

            addDocuments(document, task);

            document.add(new Paragraph(
                    "------------------------------------------------------------"));

            Paragraph footer =
                    new Paragraph(
                            "Documento gerado automaticamente pelo sistema Taciturno.",
                            FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9));

            footer.setAlignment(Paragraph.ALIGN_CENTER);

            footer.setSpacingBefore(20);

            document.add(footer);

            document.close();

            return output.toByteArray();

        } catch (DocumentException e) {

            throw new RuntimeException("Erro ao gerar PDF.", e);
        }
    }
    
    
    private void addSummary(Document document, Task task) throws DocumentException {

        Font sectionFont =
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);

        Font labelFont =
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

        Font valueFont =
                FontFactory.getFont(FontFactory.HELVETICA, 11);

        Paragraph title =
                new Paragraph("RESUMO DA DEMANDA", sectionFont);

        title.setSpacingBefore(10);
        title.setSpacingAfter(10);

        document.add(title);

        PdfPTable table = new PdfPTable(2);

        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 70});
        table.setSpacingAfter(20);

        addRow(table, "Número", String.valueOf(task.getId()), labelFont, valueFont);
        addRow(table, "Status", getStatusLabel(task), labelFont, valueFont);
        addRow(table, "Prioridade", getPriorityLabel(task), labelFont, valueFont);
        addRow(
                table,
                "Responsável",
                task.getAssignedTo() != null
                        ? task.getAssignedTo().getName()
                        : "-",
                labelFont,
                valueFont
        );

        addRow(
                table,
                "Criador",
                task.getCreatedBy() != null
                        ? task.getCreatedBy().getName()
                        : "-",
                labelFont,
                valueFont
        );

        addRow(
                table,
                "Setor",
                task.getSector() != null
                        ? task.getSector().getName()
                        : "-",
                labelFont,
                valueFont
        );

        document.add(table);
    }
    
    private void addRow(
            PdfPTable table,
            String label,
            String value,
            Font labelFont,
            Font valueFont) {

        PdfPCell left = new PdfPCell(new Phrase(label, labelFont));

        left.setBackgroundColor(new Color(235, 235, 235));
        left.setPadding(8);
        left.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell right = new PdfPCell(new Phrase(value, valueFont));

        right.setPadding(8);
        right.setVerticalAlignment(Element.ALIGN_MIDDLE);

        table.addCell(left);
        table.addCell(right);
    }
    
    private String getStatusLabel(Task task) {

        if (task.getStatus() == null) {
            return "-";
        }

        switch (task.getStatus()) {

            case PENDING:
                return "Pendente";

            case IN_PROGRESS:
                return "Em andamento";

            case COMPLETED:
                return "Concluída";

            default:
                return task.getStatus().name();
        }
    }
    
    private String getPriorityLabel(Task task) {

        if (task.getPriority() == null) {
            return "-";
        }

        switch (task.getPriority()) {

            case LOW:
                return "Baixa";

            case MEDIUM:
                return "Média";

            case HIGH:
                return "Alta";

            case URGENT:
                return "Urgente";

            default:
                return task.getPriority().name();
        }
    }
    
    private void addTitle(Document document, Task task) throws DocumentException {

        Font sectionFont =
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);

        Font titleFont =
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);

        Paragraph section =
                new Paragraph("TÍTULO", sectionFont);

        section.setSpacingBefore(5);
        section.setSpacingAfter(8);

        document.add(section);

        String title =
                task.getTitle() == null || task.getTitle().isBlank()
                        ? "Sem título"
                        : task.getTitle();

        Paragraph value =
                new Paragraph(title, titleFont);

        value.setSpacingAfter(20);

        document.add(value);
    }
    
    private void addDescription(Document document, Task task) throws DocumentException {

        Font sectionFont =
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);

        Font valueFont =
                FontFactory.getFont(FontFactory.HELVETICA, 11);

        document.add(new Paragraph(
                "DESCRIÇÃO",
                sectionFont));

        document.add(new Paragraph(" "));

        String description =
                task.getDescription() == null || task.getDescription().isBlank()
                        ? "Nenhuma descrição informada."
                        : task.getDescription();

        Paragraph paragraph =
                new Paragraph(description, valueFont);

        paragraph.setSpacingAfter(20);

        document.add(paragraph);
    }
    
    private void addHistory(Document document, Task task) throws DocumentException {

        Font sectionFont =
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);

        Paragraph title =
                new Paragraph("HISTÓRICO DA DEMANDA", sectionFont);

        title.setSpacingBefore(10);
        title.setSpacingAfter(10);

        document.add(title);

        List<TaskHistory> histories =
                taskHistoryService.findByTask(task.getId());

        if (histories.isEmpty()) {

            document.add(new Paragraph("Nenhuma movimentação registrada."));

            return;
        }

        for (TaskHistory history : histories) {

            addHistoryEntry(document, history);

        }
    }
    
    private void addHistoryEntry(
            Document document,
            TaskHistory history) throws DocumentException {

        Font dateFont =
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

        Font userFont =
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

        Font textFont =
                FontFactory.getFont(FontFactory.HELVETICA, 11);


        if (history.getCreatedAt() != null) {

            document.add(new Paragraph(
                    history.getCreatedAt().format(DATE_TIME),
                    dateFont));
        }


        if (history.getUser() != null) {

            document.add(new Paragraph(
                    history.getUser().getName(),
                    userFont));
        }


        boolean isDelegation =
                history.getDelegatedTo() != null;

        if (isDelegation) {

            String from =
                    history.getUser() != null
                            ? history.getUser().getName()
                            : "Sistema";

            document.add(new Paragraph(
                    "• "
                            + from
                            + " delegou esta demanda para "
                            + history.getDelegatedTo().getName()
                            + ".",
                    textFont));

        } else if (history.getAction() != null &&
                !history.getAction().isBlank()) {

            document.add(new Paragraph(
                    "• "
                            + formatHistoryAction(history.getAction()),
                    textFont));
        }

        if (history.getOldTitle() != null &&
                history.getNewTitle() != null &&
                !history.getOldTitle().equals(history.getNewTitle())) {


            document.add(new Paragraph(
                    "Título anterior:",
                    userFont));

            document.add(new Paragraph(
                    history.getOldTitle(),
                    textFont));


            document.add(new Paragraph(
                    "Novo título:",
                    userFont));

            document.add(new Paragraph(
                    history.getNewTitle(),
                    textFont));
        }


        if (history.getOldDescription() != null &&
                history.getNewDescription() != null &&
                !history.getOldDescription().equals(history.getNewDescription())) {


            document.add(new Paragraph(
                    "Descrição anterior:",
                    userFont));

            document.add(new Paragraph(
                    history.getOldDescription(),
                    textFont));


            document.add(new Paragraph(
                    "Nova descrição:",
                    userFont));

            document.add(new Paragraph(
                    history.getNewDescription(),
                    textFont));
        }


        if (history.getComment() != null &&
                !history.getComment().isBlank()) {

            document.add(new Paragraph(
                    "Comentário:",
                    userFont));

            document.add(new Paragraph(
                    "\"" + history.getComment() + "\"",
                    textFont));
        }
        
        Paragraph separator =
                new Paragraph(
                        "────────────────────────────────────────");

        separator.setSpacingBefore(8);
        separator.setSpacingAfter(8);

        document.add(separator);
    }
    
    private String formatHistoryAction(String action) {

        if (action == null || action.isBlank()) {
            return "-";
        }

        return action

                .replace("PENDING", "Pendente")
                .replace("IN_PROGRESS", "Em andamento")
                .replace("COMPLETED", "Concluída")

                .replace("LOW", "Baixa")
                .replace("MEDIUM", "Média")
                .replace("HIGH", "Alta")
                .replace("URGENT", "Urgente");
    }
    
    private void addDocuments(
            Document document,
            Task task) throws DocumentException {

        Font sectionFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        14);

        Font valueFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA,
                        11);

        document.add(new Paragraph(
                "DOCUMENTOS DA DEMANDA",
                sectionFont));

        document.add(new Paragraph(" "));

        List<Attachment> attachments =
                attachmentService.findByTask(task.getId());

        document.add(new Paragraph(
                "Total de documentos: "
                        + attachments.size(),
                valueFont));

        document.add(new Paragraph(" "));

        if (attachments.isEmpty()) {

            document.add(new Paragraph(
                    "Nenhum documento anexado.",
                    valueFont));

            document.add(new Paragraph(" "));

            return;
        }

        // Continuaremos daqui
        for (Attachment attachment : attachments) {
        	
        	addDocumentEntry(document, attachment);
        }
        
        
    }
    
   

private void addDocumentEntry(
        Document document,
        Attachment attachment) throws DocumentException {

    Font nameFont =
            FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    11);

    Font valueFont =
            FontFactory.getFont(
                    FontFactory.HELVETICA,
                    10);

    document.add(new Paragraph(
            attachment.getOriginalFileName(),
            nameFont));

    document.add(new Paragraph(
            formatFileSize(attachment.getFileSize()),
            valueFont));

    if (attachment.getUploadedAt() != null) {

        document.add(new Paragraph(
                "Anexado em: "
                        + attachment.getUploadedAt().format(DATE_TIME),
                valueFont));
    }

    document.add(new Paragraph(
            "--------------------------------------------"));

    document.add(new Paragraph(" "));
}


private String formatFileSize(Long bytes) {

    if (bytes == null) {
        return "-";
    }

    if (bytes < 1024) {
        return bytes + " B";
    }

    if (bytes < 1024 * 1024) {
        return String.format("%.1f KB", bytes / 1024.0);
    }

    return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    
  }
}