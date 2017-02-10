package com.sam.hab.util.txrx;

public class TwoWayPacketGenerator {

    /**
     * Generates a shell response packet, used by the payload to encode the result of a command executed in response to a remote shell command packet.
     * @param callsign the payload callsign.
     * @param response the result of executing the command.
     * @return an array of the packets which are to be transmitted to send the response.
     */
    public static String[] generateShellPackets(String callsign, String[] response) {
        for (int i = 0; i < response.length; i++) {
            String pckt = ">>" + callsign + ",%s,1," + response[i].replace('\n', (char)0);
            response[i] = pckt;
        }
        return response;
    }

    /**
     * Generates a shell command packet to be sent to the payload to cause remote execution of a shell command.
     * @param callsign payload callsign.
     * @param cmd the command to execute, cannot include asterisks.
     * @return the packet.
     */
    public static String generateShellCmdPacket(String callsign, String cmd) {
        return ">>" + callsign + ",%s,1," + cmd;
    }

    /**
     * Generates a stat packet, this is sent by the payload in response to a statistic request by the ground station.
     * @param callsign payload callsign.
     * @param name the name of the stat/
     * @param stat the stat.
     * @return the packet.
     */
    public static String generateStatPacket(String callsign, String name, String stat) {
        return ">>" + callsign + ",%s,2," + name + "/" + stat;
    }

    /**
     * Generates a command packet, this is a packet that it sent to the payload requesting that it do something, this could be RBT (reboot) or many other things.
     * This is also used once by the payload to generate the TRA packet that enables the ground station to begin transmitting.
     * @param callsign payload callsign.
     * @param cmd the command.
     * @return the packet.
     */
    public static String generateCommand(String callsign, String cmd) {
        return ">>" + callsign + ",%s,0," + cmd;
    }
}