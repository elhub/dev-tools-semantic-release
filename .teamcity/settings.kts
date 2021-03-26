import jetbrains.buildServer.configs.kotlin.v2019_2.BuildFeatures
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.DslContext
import jetbrains.buildServer.configs.kotlin.v2019_2.Trigger
import jetbrains.buildServer.configs.kotlin.v2019_2.VcsRoot
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.SshAgent
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.project
import jetbrains.buildServer.configs.kotlin.v2019_2.sequential
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.VcsTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.version
import no.elhub.common.build.configuration.SonarScan
import no.elhub.common.build.configuration.UnitTestGradle
import no.elhub.common.build.configuration.AssembleGradle
import no.elhub.common.build.configuration.AutoRelease
import no.elhub.common.build.configuration.constants.GlobalTokens

version = "2020.2"

project {

    params {
        param("teamcity.ui.settings.readOnly", "true")
    }

    val bitbucketAuth = BuildFeatures()
    bitbucketAuth.feature(
        SshAgent {
            teamcitySshKey = "teamcity_git_rsa"
            param("secure:passphrase", GlobalTokens.bitbucketSshPassphrase)
        })


    val buildChain = sequential {

        buildType(
            UnitTestGradle(
                UnitTestGradle.Config(
                    vcsRoot = DslContext.settingsRoot
                )
            )
        )

        buildType(
            SonarScan(
                SonarScan.Config(
                    vcsRoot = DslContext.settingsRoot,
                    sonarId = "no.elhub.tools:dev-tools-auto-release",
                    sonarProjectSources = "src"
                )
            )
        )

        buildType(
            AssembleGradle(
                AssembleGradle.Config(
                    vcsRoot = DslContext.settingsRoot
                )
            )
        )

        buildType(
            AutoRelease(
                AutoRelease.Config(
                    vcsRoot = DslContext.settingsRoot,
                    trigger = VcsTrigger(),
                    buildFeatures = bitbucketAuth
                )
            )
        )

    }

    buildChain.buildTypes().forEach { buildType(it) }

}
