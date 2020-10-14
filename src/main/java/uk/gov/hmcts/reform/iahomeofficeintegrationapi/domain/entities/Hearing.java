package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

public class Hearing {

    private String hmctsHearingRef;
    private String hearingLocation;
    private String hearingDate;
    private String hearingTime;
    private String hearingType;
    private int witnessQty;
    private String witnessNames;

    public Hearing(String hmctsHearingRef, String hearingLocation, String hearingDate, String hearingTime,
                   String hearingType, int witnessQty, String witnessNames) {

        this.hmctsHearingRef = hmctsHearingRef;
        this.hearingLocation = hearingLocation;
        this.hearingDate = hearingDate;
        this.hearingTime = hearingTime;
        this.hearingType = hearingType;
        this.witnessQty = witnessQty;
        this.witnessNames = witnessNames;
    }

    private Hearing() {
    }

    public String getHmctsHearingRef() {
        return hmctsHearingRef;
    }

    public String getHearingLocation() {
        return hearingLocation;
    }

    public String getHearingDate() {
        return hearingDate;
    }

    public String getHearingTime() {
        return hearingTime;
    }

    public String getHearingType() {
        return hearingType;
    }

    public int getWitnessQty() {
        return witnessQty;
    }

    public String getWitnessNames() {
        return witnessNames;
    }


    public static final class HearingBuilder {
        private String hmctsHearingRef;
        private String hearingLocation;
        private String hearingDate;
        private String hearingTime;
        private String hearingType;
        private int witnessQty;
        private String witnessNames;

        private HearingBuilder() {
        }

        public static HearingBuilder hearing() {
            return new HearingBuilder();
        }

        public HearingBuilder withHmctsHearingRef(String hmctsHearingRef) {
            this.hmctsHearingRef = hmctsHearingRef;
            return this;
        }

        public HearingBuilder withHearingLocation(String hearingLocation) {
            this.hearingLocation = hearingLocation;
            return this;
        }

        public HearingBuilder withHearingDate(String hearingDate) {
            this.hearingDate = hearingDate;
            return this;
        }

        public HearingBuilder withHearingTime(String hearingTime) {
            this.hearingTime = hearingTime;
            return this;
        }

        public HearingBuilder withHearingType(String hearingType) {
            this.hearingType = hearingType;
            return this;
        }

        public HearingBuilder withWitnessQty(int witnessQty) {
            this.witnessQty = witnessQty;
            return this;
        }

        public HearingBuilder withWitnessNames(String witnessNames) {
            this.witnessNames = witnessNames;
            return this;
        }

        public Hearing build() {
            return
                new Hearing(
                    hmctsHearingRef, hearingLocation, hearingDate,
                    hearingTime, hearingType, witnessQty, witnessNames
                );
        }
    }
}
