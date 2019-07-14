package za.co.dubedivine.networks.repository

import org.springframework.data.mongodb.repository.MongoRepository
import za.co.dubedivine.networks.model.VoteEntityBridge

interface VoteEntityBridgeRepository : MongoRepository<VoteEntityBridge, Pair<String, String>>