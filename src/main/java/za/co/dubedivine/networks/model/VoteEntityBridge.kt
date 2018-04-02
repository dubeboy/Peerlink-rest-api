package za.co.dubedivine.networks.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

// third class is a bridge between an voted entiny
// isVoteTheSameDirection is the person trying to voteUp again ???
@Document
data class VoteEntityBridge(@Id @Indexed var id: Pair<String, String>?, var isVoteTheSameDirection: Boolean?)

