package com.pgManagement.roomservice.entity;

    public enum BedStatus {

        AVAILABLE("AVAILABLE", "Available"),
        OCCUPIED("OCCUPIED", "Occupied"),
        MAINTENANCE("MAINTENANCE", "Under Maintenance"),
        BLOCKED("BLOCKED", "Blocked"),
        TO_BE_VACANT("TO_BE_VACANT", "To Be Vacant");
        private final String code;
        private final String label;

        BedStatus(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() {
            return code;
        }

        public String getLabel() {
            return label;
        }

}
