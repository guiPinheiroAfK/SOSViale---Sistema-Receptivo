package br.com.sosviale.offline.dto;

public class PendingOperationDto {

    public enum Type { UPDATE_TRANSFER_STATUS, DELETE_TRANSFER }

    private String id;
    private Type type;
    private Integer transferId;
    private String status;
    private String createdAt;

    public PendingOperationDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public Integer getTransferId() { return transferId; }
    public void setTransferId(Integer transferId) { this.transferId = transferId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
