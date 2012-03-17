package archimulator.util;

import archimulator.sim.base.experiment.profile.ExperimentProfile;
import com.jcraft.jsch.*;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class UpdateHelper {
    private static void getDir(ChannelSftp channel, String remoteFile, File localFile) throws IOException, SftpException {
        String pwd = remoteFile;
        if (remoteFile.lastIndexOf('/') != -1) {
            if (remoteFile.length() > 1) {
                pwd = remoteFile.substring(0, remoteFile.lastIndexOf('/'));
            }
        }
        channel.cd(pwd);
        if (!localFile.exists()) {
            localFile.mkdirs();
        }
        java.util.Vector files = channel.ls(remoteFile);
        for (int i = 0; i < files.size(); i++) {
            ChannelSftp.LsEntry le = (ChannelSftp.LsEntry) files.elementAt(i);
            String name = le.getFilename();
            if (le.getAttrs().isDir()) {
                if (name.equals(".") || name.equals("..")) {
                    continue;
                }
                getDir(channel,
                        channel.pwd() + "/" + name + "/",
                        new File(localFile, le.getFilename()));
            } else {
                getFile(channel, le, localFile);
            }
        }
        channel.cd("..");
    }

    private static void getFile(ChannelSftp channel, ChannelSftp.LsEntry le, File localFile) throws IOException, SftpException {
        String remoteFile = le.getFilename();
        if (!localFile.exists()) {
            String path = localFile.getAbsolutePath();
            int i = path.lastIndexOf(File.pathSeparator);
            if (i != -1) {
                if (path.length() > File.pathSeparator.length()) {
                    new File(path.substring(0, i)).mkdirs();
                }
            }
        }

        if (localFile.isDirectory()) {
            localFile = new File(localFile, remoteFile);
        }

        System.out.printf("[%s Updating client files] receiving: %s (%d bytes)%n", DateHelper.toString(new Date()), remoteFile, le.getAttrs().getSize());
        channel.get(remoteFile, localFile.getAbsolutePath());
    }

    public static void update() {
        System.out.printf("[%s Begin Updating client files]%n", DateHelper.toString(new Date()));

        JSch jsch = new JSch();
        Session session;
        try {
            session = jsch.getSession(DeploymentProfile.getCurrent().getServerUserId(), DeploymentProfile.getCurrent().getServerIpv4Address(), 22);

            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(DeploymentProfile.getCurrent().getServerUserPassword());
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;

            sftpChannel.cd(DeploymentProfile.getCurrent().getArchimulatorDirectoryPath());
            getDir(sftpChannel, ".", new File(ExperimentProfile.getUserHome() + "/Archimulator"));

            sftpChannel.exit();
            session.disconnect();

            System.out.printf("[%s Updated client files]%n", DateHelper.toString(new Date()));
        } catch (JSchException e) {
            e.printStackTrace();
            System.out.printf("[%s Failed to update client files]%n", DateHelper.toString(new Date()));
            System.exit(-1);
        } catch (SftpException e) {
            e.printStackTrace();
            System.out.printf("[%s Failed to update client files]%n", DateHelper.toString(new Date()));
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.printf("[%s Failed to update client files]%n", DateHelper.toString(new Date()));
            System.exit(-1);
        }
    }
}
