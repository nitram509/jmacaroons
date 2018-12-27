package com.github.nitram509.jmacaroons;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.nitram509.jmacaroons.util.Base64;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
// The ordering is so that we can compare at least a part of the base64 encoding
@JsonPropertyOrder({"v", "l", "i", "i64", "c", "s", "s64"})
public class MacaroonJSONV2 {

    private String location;
    private String identifier;
    private String identifier64;
    private String signature;
    private String signature64;
    private List<CaveatJSONV2> caveats;
    private final int version;

    public MacaroonJSONV2() {
        this.location = "";
        this.identifier = "";
        this.identifier64 = "";
        this.signature = "";
        this.signature64 = "";
        this.caveats = new ArrayList<>();
        this.version = 2;
    }

    @JsonProperty("l")
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @JsonProperty("i")
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @JsonProperty("i64")
    public String getIdentifier64() {
        return identifier64;
    }

    public void setIdentifier64(String identifier64) {
        this.identifier64 = identifier64;
    }

    @JsonProperty("s")
    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @JsonProperty("s64")
    public String getSignature64() {
        return signature64;
    }

    public void setSignature64(String signature64) {
        this.signature64 = signature64;
    }

    @JsonProperty("c")
    public List<CaveatJSONV2> getCaveats() {
        return caveats;
    }

    public void setCaveats(List<CaveatJSONV2> caveats) {
        this.caveats = caveats;
    }

    @JsonProperty("v")
    public int getVersion() {
        return version;
    }

    // Get the optionally base64 encoded values

    @JsonIgnore
    public byte[] parseSignature() {
        // If the signature is empty, base64 decode the s64 value
        if (this.getSignature().equals(""))  {
            return Base64.decode(this.getSignature64());
        }
        return this.getSignature().getBytes();
    }

    @JsonIgnore
    public String parseIdentifier() {
        if (this.getIdentifier().equals("")) {
            return new String(Base64.decode(this.getIdentifier64()));
        }
        return this.getIdentifier();
    }

    @JsonIgnore
    public CaveatPacket[] getCaveatPackets() {
        List<CaveatPacket> packets = new ArrayList<>(this.caveats.size() * 3);

        for (final CaveatJSONV2 caveat : this.caveats) {
            packets.addAll(caveat.toPackets());
        }

        return packets.toArray(new CaveatPacket[0]);
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonPropertyOrder({"l", "i", "i64", "v", "v64"})
    public static class CaveatJSONV2 {


        private String CID;
        private String CID64;
        private String VID;
        private String VID64;
        private String Location;

        public CaveatJSONV2() {
            this.CID = "";
            this.CID64 = "";
            this.VID = "";
            this.VID64 = "";
            this.Location = "";
        }

        @JsonProperty("i")
        public String getCID() {
            return CID;
        }

        public void setCID(String CID) {
            this.CID = CID;
        }

        @JsonProperty("i64")
        public String getCID64() {
            return CID64;
        }

        public void setCID64(String CID64) {
            this.CID64 = CID64;
        }

        @JsonProperty("v")
        public String getVID() {
            return VID;
        }

        public void setVID(String VID) {
            this.VID = VID;
        }

        @JsonProperty("v64")
        public String getVID64() {
            return VID64;
        }

        public void setVID64(String VID64) {
            this.VID64 = VID64;
        }

        @JsonProperty("l")
        public String getLocation() {
            return Location;
        }

        public void setLocation(String location) {
            Location = location;
        }

        // Get the optionally base64 encoded values

        @JsonIgnore
        public byte[] parseSignature() {
            // If the signature is empty, base64 decode the s64 value
            if (this.getVID().equals(""))  {
                return Base64.decode(this.getVID64());
            }
            return this.getVID().getBytes();
        }

        @JsonIgnore
        public String parseIdentifier() {
            if (this.getCID().equals("")) {
                return new String(Base64.decode(this.getCID64()));
            }
            return this.getCID();
        }

        @JsonIgnore
        public List<CaveatPacket> toPackets() {
            List<CaveatPacket> packets = new ArrayList<>(3);

            // Order is important because the Macaroon.equals method checks that the values are in the same order.
            // ID
            packets.add(new CaveatPacket(CaveatPacket.Type.cid, this.parseIdentifier()));
            // Signature (optional)
            if (this.parseSignature().length != 0){
                packets.add(new CaveatPacket(CaveatPacket.Type.vid, this.parseSignature()));
            }
            // Location (optional)
            if (!this.getLocation().equals("")) {
                packets.add(new CaveatPacket(CaveatPacket.Type.cl, this.getLocation()));
            }
            return packets;
        }
    }
}
