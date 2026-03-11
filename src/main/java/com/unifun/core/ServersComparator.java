package com.unifun.core;

public class ServersComparator {
    /*public static void compareServers(String path) throws IOException {
        Set<String> hostOverviewServers = new HashSet<>(HTMLParser.getServersFromHostOverview());
        Set<String> raidServers = new HashSet<>(DataReader.getServerData(path).keySet());

        Set<String> difference = new HashSet<>(hostOverviewServers);
        difference.removeAll(raidServers);

        String nonRaid = difference.stream()
                .reduce(new StringBuilder(),
                        (sb, s) -> sb.append(s).append("\n"),
                        StringBuilder::append)
                .toString();

        String raid = raidServers.stream()
                .reduce(new StringBuilder(),
                        (sb, s) -> sb.append(s).append("\n"),
                        StringBuilder::append)
                .toString();

        String hostOverview = hostOverviewServers.stream()
                .reduce(new StringBuilder(),
                        (sb, s) -> sb.append(s).append("\n"),
                        StringBuilder::append)
                .toString();

        Files.writeString(Path.of(AppConfig.get("html.output.non-raid")), nonRaid);
        Files.writeString(Path.of(AppConfig.get("html.output.raid")), raid);
        Files.writeString(Path.of(AppConfig.get("html.output.host-overview")), hostOverview);

        System.out.println("Complete! Server data was written in files");
    }*/
}
