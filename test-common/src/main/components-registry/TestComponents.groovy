"main-component-first" {
    system = "CLASSIC"
    componentDisplayName = "Main component first"
    componentOwner = "jdoe"
    releaseManager = "jdoe"
    groupId = "corp.domain"
    vcsUrl = "ssh://git@git.domain.corp/main/main-component-first.git"
    solution = false
    jira {
        projectKey = 'MAIN'
        lineVersionFormat = '$major.$minor'
        majorVersionFormat = '$major.$minor'
        releaseVersionFormat = '$major.$minor.$service'
        displayName = 'Main component first'
    }
    distribution {
        external = true
        explicit = true
    }
}

"main-component-second" {
    system = "NONE"
    componentDisplayName = "Main component second"
    componentOwner = "jdoe"
    releaseManager = "jdoe"
    groupId = "corp.domain"
    vcsUrl = "ssh://git@git.domain.corp/main/main-component-second.git"
    solution = false
    jira {
        projectKey = 'MAIN'
        lineVersionFormat = '$major.$minor'
        majorVersionFormat = '$major.$minor'
        releaseVersionFormat = '$major.$minor.$service'
        displayName = 'Main component second'
    }
    distribution {
        external = true
        explicit = true
    }
}

"main-component-third" {
    system = "CLASSIC"
    componentDisplayName = "Main component third"
    componentOwner = "jdoe"
    releaseManager = "jdoe"
    groupId = "corp.domain"
    vcsUrl = "ssh://git@git.domain.corp/main/main-component-third.git"
    solution = false
    jira {
        projectKey = 'MAIN'
        lineVersionFormat = '$major.$minor'
        majorVersionFormat = '$major.$minor'
        releaseVersionFormat = '$major.$minor.$service'
        displayName = 'Main component third'
    }
    distribution {
        external = true
        explicit = true
    }
}

"main-component-solution" {
    system = "CLASSIC"
    componentDisplayName = "Main component solution"
    componentOwner = "jdoe"
    releaseManager = "jdoe"
    groupId = "corp.domain"
    vcsUrl = "ssh://git@git.domain.corp/main/main-component-solution.git"
    solution = true
    jira {
        projectKey = 'MAIN'
        lineVersionFormat = '$major.$minor'
        majorVersionFormat = '$major.$minor'
        releaseVersionFormat = '$major.$minor.$service'
        displayName = 'Main component solution'
    }
    distribution {
        external = true
        explicit = true
    }
}

"main-component-not-external" {
    system = "NONE"
    componentDisplayName = "Main component not external"
    componentOwner = "jdoe"
    releaseManager = "jdoe"
    groupId = "corp.domain"
    vcsUrl = "ssh://git@git.domain.corp/main/main-component-ne.git"
    solution = false
    jira {
        projectKey = 'MAIN'
        lineVersionFormat = '$major.$minor'
        majorVersionFormat = '$major.$minor'
        releaseVersionFormat = '$major.$minor.$service'
        displayName = 'Main component not external'
    }
    distribution {
        external = false
        explicit = true
    }
}

"dependency-component-first" {
    system = "NONE"
    componentDisplayName = "Dependency component first"
    componentOwner = "jdoe"
    releaseManager = "jdoe"
    groupId = "corp.domain"
    vcsUrl = "ssh://git@git.domain.corp/dep/dependency-component-first.git"
    solution = false
    jira {
        projectKey = 'DEP'
        lineVersionFormat = '$major.$minor'
        majorVersionFormat = '$major.$minor'
        releaseVersionFormat = '$major.$minor.$service'
        displayName = "Dependency component first"
    }
    distribution {
        external = true
        explicit = true
    }
}