# AbyssVideoDownloader

**AbyssVideoDownloader** is a command-line Kotlin application that allows users to download Abyss.to videos. It supports downloading videos in different resolutions.

## Prerequisites

Before using AbyssVideoDownloader, ensure you have Java Development Kit (JDK 21) is installed.

### Installation Instructions for JDK 21

<details>
    <summary>Linux</summary>


To install JDK 21 on a Linux machine using the command line:

1. **Open Terminal** on your Linux machine.
2. **Update package index**:

   ```bash
   sudo apt update
   ```

3. **Install OpenJDK 21** (if available via your package manager):

   ```bash
   sudo apt install openjdk-21-jdk
   ```

   If JDK 21 is not available in your distribution’s package manager, you can manually download and install it:

    1. **Download JDK 21** from [Oracle's official site](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html).

    2. **Extract the downloaded archive** (replace `jdk-21_linux-x64_bin.tar.gz` with the actual file name):

       ```bash
       tar -xvzf jdk-21_linux-x64_bin.tar.gz
       ```

    3. **Move the JDK folder** to `/opt`:

       ```bash
       sudo mv jdk-21 /opt/
       ```

    4. **Set environment variables**:

       ```bash
       sudo update-alternatives --install /usr/bin/java java /opt/jdk-21/bin/java 1
       sudo update-alternatives --install /usr/bin/javac javac /opt/jdk-21/bin/javac 1
       ```

    5. **Verify the installation**:

       ```bash
       java -version
       ```

  </details>

<details>
    <summary>Windows</summary>


To install JDK 21 on a Windows machine:

1. **Download JDK 21** from [Oracle's official site](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html).
2. **Run the installer** and follow the on-screen instructions.
3. During the installation, make sure to select the option to **add Java to your system’s PATH**.
4. **Verify the installation**:

    - Open **Command Prompt** (`cmd`).
    - Type the following command:

      ```cmd
      java -version
      ```

If command return the version number (e.g., `java 21`), then the installation was successful.

</details>


## Getting the JAR File

You have two options for obtaining the `abyss-dl.jar` file:

1. **Download the Latest JAR File**:
   - You can download the latest `abyss-dl.jar` file from the [Releases](https://github.com/abdlhay/AbyssVideoDownloader/releases) section of this repository.

2. **Build the JAR File Manually**:
   - If you prefer to build the project yourself or want the latest updates, follow the instructions in the [**Building the Project**](https://github.com/abdlhay/AbyssVideoDownloader#building-the-project) section below.


## Usage

### Downloading a Video with AbyssVideoDownloader

To download a video, follow these steps:

1. **Run the Command**:
   Open your terminal and enter the following command:

   ```bash
   java -jar abyss-dl.jar [-H <header>] [--header <header>] [-o <output_file_path>] [-c <number_of_connections>]
   ```
- **Add HTTP Headers** (Optional):
    - Use `-H <header>` or `--header <header>` to include additional information with your request.
    - Example: `-H "Authorization: Bearer TOKEN"`
    - You can add multiple headers by repeating the `-H` or `--header` option.

&nbsp;

- **Specify Output File** (Optional):
    - Use `-o <output_file_path>` to choose where to save the downloaded video.
    - Example: `-o /path/to/my_video.mp4`
    - If you don’t specify a path, the video will be saved in the current directory with a default name.

&nbsp;
- **Set Number of Connections** (Optional):
    - Use `-c <number_of_connections>` or `--connections <number_of_connections>` to set how many connections to use for downloading. This can help speed up the download.
    - The number must be between 1 and 10; by default, it is set to 4.
    - Example: `-c 7`

&nbsp;

- **Verbose Mode** (Optional):
    - Use the `--verbose` flag to enable verbose output.


### Examples

1. **Download a video and save it with a specific name in a chosen folder**:
   ```bash
   java -jar abyss-dl.jar -o /path/to/directory/my_video.mp4
   ```

2. **Download a video and save it with the default name in the current directory**:
   ```bash
   java -jar abyss-dl.jar
   ```

3. **Download a video using custom headers**:
   ```bash
   java -jar abyss-dl.jar -H "Authorization: Bearer TOKEN" --header "Referer: https://example.com" -o my_video.mp4
   ```

4. **Download a video with multiple connections**:
   ```bash
   java -jar abyss-dl.jar -c 7 -o my_video.mp4
   ```

5. **Download a video using custom headers and multiple connections**:
   ```bash
   java -jar abyss-dl.jar -H "Authorization: Bearer TOKEN" --header "Referer: https://example.com" -c 3 -o /path/to/my_video.mp4
   ```

6. After running the command, you will be prompted for the video URL:

   ```
   enter the video URL or ID: https://abysscdn.com/?v=K8R6OOjS7
   ```

## Building the Project

You can build the AbyssVideoDownloader project using GitHub Actions by following these steps:

### Steps to Build the Project

1. **Fork the Repository**:
    - Click the **"Fork"**.

2. **Open the Actions Tab**:
    - Navigate to your forked repository and click on the **"Actions"** tab.

3. **Run the Build Workflow**:
    - Find the build workflow `build AbyssVideoDownloader`, click on it, and then select **"Run workflow"**.

4. **Access the Releases Section**:
    - After the build completes, navigate to the **Releases** section of your repository. The JAR file, `abyss-dl.jar`, will be available for download there.

## TODOs

Here are the planned tasks and features for future updates:

- [ ] Clean Code.
- [x] Add support for multiple parallel downloads.
- [ ] (Maybe) Integrate dependency injection
- [ ] Improve error handling and provide more descriptive messages for common issues.
- [ ] Implement retry and resume logic for failed downloads.
- [x] Enhance logging with different verbosity levels (e.g., debug, info, error).
- [ ] Add support for proxy configuration.
- [ ] Add a graphical user interface (GUI) for easier interaction or maybe an android app

Feel free to contribute by picking any task or suggesting new ones!