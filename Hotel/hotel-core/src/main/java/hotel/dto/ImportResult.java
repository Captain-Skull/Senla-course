package hotel.dto;

import java.util.List;

public class ImportResult {

    private int total;
    private int imported;
    private java.util.List<FailedImportItem> failed = new java.util.ArrayList<>();
    private java.util.List<String> warnings = new java.util.ArrayList<>();

    public ImportResult() {
    }

    public ImportResult(int total, int imported) {
        this.total = total;
        this.imported = imported;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getImported() {
        return imported;
    }

    public void setImported(int imported) {
        this.imported = imported;
    }

    public List<FailedImportItem> getFailed() {
        return failed;
    }

    public void setFailed(List<FailedImportItem> failed) {
        this.failed = failed;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public void addFailed(FailedImportItem item) {
        this.failed.add(item);
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
}

