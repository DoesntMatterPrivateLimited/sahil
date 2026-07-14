def call(String branch = 'main', String repoUrl) {
    echo "Checking out branch: ${branch} from ${repoUrl}"
    git branch: branch, url: repoUrl
}