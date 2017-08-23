package za.co.dubedivine.networks.util

object KUtils {
    enum class CONTENT_TYPE {
        VID_IMG, DOC
    }

    fun getFileType(mime: String) : CONTENT_TYPE {
        if(mime.startsWith("image") || mime.startsWith("video")) {
            return CONTENT_TYPE.VID_IMG
        } else {
            return CONTENT_TYPE.DOC
        }
    }
}