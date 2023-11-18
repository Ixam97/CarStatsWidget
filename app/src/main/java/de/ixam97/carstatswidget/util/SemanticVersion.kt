package de.ixam97.carstatswidget.util

class SemanticVersion {
    val major: Int
    val minor: Int
    val patch: Int

    companion object {
        /**
         * Returns true if v1 is smaller than v2.
         */
        fun compareVersions(v1: SemanticVersion, v2: SemanticVersion): Boolean {
            return if (v2.major > v1.major) {
                true
            } else if (v2.major < v1.major) {
                false
            } else {
                if (v2.minor > v1.minor) {
                    true
                } else if (v2.minor < v1.minor) {
                    false
                } else {
                    v2.patch > v1.patch
                }
            }
        }
    }

    constructor(versionString: String) {
        val splitString = versionString.split('.')
        major = splitString[0].toInt()
        minor = splitString[1].toInt()
        patch = splitString[2].toInt()
    }
    constructor(pMajor: Int, pMinor: Int, pPatch: Int) {
        major = pMajor
        minor = pMinor
        patch = pPatch
    }

    fun getStringVersion() = "$major.$minor.$patch"
}