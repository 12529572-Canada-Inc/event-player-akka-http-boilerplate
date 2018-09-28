package my.artifact.myeventplayer.common.command

import co.remotectrl.eventplayer.*
import my.artifact.myeventplayer.common.aggregate.MyAggregate
import my.artifact.myeventplayer.common.event.MyChangedEvent

open class MyChangeCommand(var myChangeVal: String) : PlayCommand<MyAggregate> {

    constructor() : this("")

    override fun validate(model: MyAggregate) {
        if(myChangeVal.isEmpty()){
            throw Exception("Invalid commit input")
        }
    }

    override fun getEvent(aggregateId: AggregateId<MyAggregate>, version: Int): PlayEvent<MyAggregate> {

        //todo: generate unique id
        return MyChangedEvent(EventLegend(EventId(0), aggregateId, version), myChangeVal)
    }
}