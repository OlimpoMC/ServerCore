package br.com.brunoxkk0.servercore.api.downloader;

import java.io.File;
import java.net.URL;

public interface IDownloadable {

    File getFinalFolder();

    String getTargetFileName();

    String getTargetFileExtension();

    URL getTargetURL();

}
