package RecyclerView.RecyclerView.Moduls

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


/*
* Singleton Pattern
 */
class EventRepository() : DataCallback<Event>, DataCallback2<Event>, DataCallBack3<Event> {

    lateinit var instance: EventRepository
    private var dataset: ArrayList<Event> = ArrayList()
    private var dataset2: ArrayList<Event> = ArrayList()
    private var dataset3: ArrayList<Event> = ArrayList()

    var ref = FirebaseDatabase.getInstance()
        .getReference("Event") //Henter referanse til det du skriver inn


    fun getTheInstance(): EventRepository {
        instance = EventRepository()
        return instance
    }


    fun leggTilEvent(event: Event) {
        ref = FirebaseDatabase.getInstance()
            .getReference("Event") //Henter referanse til det du skriver inn
        val eventID = ref.push().key // Lager en unik id som kan brukes i objektet

        event.eventID = eventID!!

        ref.child(eventID).setValue(event).addOnCompleteListener {
            //Noe kan skje når databsen er ferdig lastet inn
        }
    }

    fun slettEvent(event: Event){
        ref = FirebaseDatabase.getInstance()
            .getReference("Event") //Henter referanse til det du skriver inn

        ref.child(event.eventID).removeValue()
    }

    fun avsluttPåmeldt(innloggetID: String, eventID: String, erPåmeldt: Boolean){
        var ref = FirebaseDatabase.getInstance()
            .getReference("Påmeld")

        ref.orderByChild("innloggetID").equalTo(innloggetID).addListenerForSingleValueEvent(object :
            ValueEventListener {

            //Inneholder alle verdier fra tabellen
            override fun onDataChange(p0: DataSnapshot) {

                if (p0!!.exists()) {


                    for (pml in p0.children) {
                        val påmeldt = pml.getValue(Påmeld::class.java)

                        if (påmeldt!!.event.eventID == eventID) {
                            ref.child(pml.key!!).removeValue()
                        }
                    }
                    økMedlemer(eventID, erPåmeldt)
                }

            }

            override fun onCancelled(p0: DatabaseError) {
                Log.i("lala", "NOE GIKK FEIL MED DATABASEKOBLING!")
            }

        })
    }

    fun updateEvent(event: Event){
        ref = FirebaseDatabase.getInstance()
            .getReference("Event") //Henter referanse til det du skriver inn


        ref.child(event.eventID).setValue(event).addOnCompleteListener {
            //Noe kan skje når databsen er ferdig lastet inn
        }
    }

    fun påmeldEvent(påmeld: Påmeld, erPåmeldt: Boolean){
        ref = FirebaseDatabase.getInstance()
            .getReference("Påmeld") //Henter referanse til det du skriver inn

        val eventID = ref.push().key!! // Lager en unik id som kan brukes i objektet

        ref.child(eventID).setValue(påmeld).addOnCompleteListener {
            //Noe kan skje når databsen er ferdig lastet inn
            økMedlemer(påmeld.event.eventID, erPåmeldt)
        }
    }

    fun økMedlemer(eventID: String, erPåmeldt: Boolean){

        ref = FirebaseDatabase.getInstance()
            .getReference("Event").child(eventID).child("antPåmeldte")

       // ref.setValue("10")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var ant = dataSnapshot.getValue(String::class.java)

                var nr: Int = Integer.parseInt(ant.toString())


                if(erPåmeldt)
                    nr = nr -1
                else nr = nr +1

                Log.i("lala","RegneStykke: " +(nr))
                ref.setValue(""+(nr))
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("lala", "onCancelled", databaseError.toException())
            }
        })

//        ref.addValueEventListener(object: ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot!!.exists()) {
//                    Log.i("lala","EXISTS")
//
//                    for (evnt in snapshot.children) {
//                        val event = evnt.getValue(Event::class.java)
////
////                        val antPåmeldte = event!!.antPåmeldte.toInt()
////                        Log.i("lala","Meldt på event " + antPåmeldte)
////                        val map = HashMap<String, Any>()
////                        map["antPåmeldte"] = ""+(antPåmeldte + 1)
////                        ref.updateChildren(map)
//
//                    }
//
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.i("lala", "NOE GIKK FEIL MED DATABASEKOBLING!")
//            }
//
//        })

    }


    fun getEvents(type: Int): MutableLiveData<List<Event>> {
        createDataset2(type)

        var data:MutableLiveData<List<Event>> = MutableLiveData()
        data.setValue(dataset)
        //Log.i("lala", "Test dataset: " + (dataset.value?.get(0) ?: " Empty" ))
        return data
    }

    fun getPåmeldteEvents(type: Int, id: String): MutableLiveData<List<Event>> {
        finnPåmeldteEvents(type, id)

        var data:MutableLiveData<List<Event>> = MutableLiveData()
        data.setValue(dataset3)
        //Log.i("lala", "Test dataset: " + (dataset.value?.get(0) ?: " Empty" ))
        return data
    }

    fun getLagdeEvents(type: Int, id: String): MutableLiveData<List<Event>> {
        finnLagdeEvents(id)

        var data:MutableLiveData<List<Event>> = MutableLiveData()
        data.setValue(dataset2)
        //Log.i("lala", "Test dataset: " + (dataset.value?.get(0) ?: " Empty" ))
        return data
    }

    fun finnPåmeldteEvents(type: Int, id: String){
        ref = FirebaseDatabase.getInstance()
            .getReference("Påmeldt") //Henter referanse til det du skriver inn

        ref.orderByChild("innloggetID").equalTo(id).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val arr: ArrayList<Event> = ArrayList()
                if (snapshot!!.exists()) {

                    for (evnt in snapshot.children) {
                        val event = evnt.getValue(Event::class.java)
                        event!!.viewType = type
                        arr.add(event!!)
                    }

                    onCallBack3(arr)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("lala", "NOE GIKK FEIL MED DATABASEKOBLING!")
            }
        })

    }

    fun finnLagdeEvents(personID: String){
        ref = FirebaseDatabase.getInstance()
            .getReference("Event") //Henter referanse til det du skriver inn

        ref.orderByChild("forfatter").equalTo(personID).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val arr: ArrayList<Event> = ArrayList()
                if (snapshot!!.exists()) {

                    for (evnt in snapshot.children) {
                        val event = evnt.getValue(Event::class.java)
                        arr.add(event!!)
                    }

                    onCallBack2(arr)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("lala", "NOE GIKK FEIL MED DATABASEKOBLING!")
            }
        })

    }

    //Henter tabellen fra firebase og returner den
            fun createDataset2(type: Int) {
                //setIt("Base create")
        ref = FirebaseDatabase.getInstance()
            .getReference("Event") //Henter referanse til det du skriver inn

                ref.addValueEventListener(object : ValueEventListener {

                    //Inneholder alle verdier fra tabellen
                    override fun onDataChange(p0: DataSnapshot) {
                        val arr: ArrayList<Event> = ArrayList()
                        if (p0!!.exists()) {

                            for (evt in p0.children) {
                                val event = evt.getValue(Event::class.java)
                                event!!.viewType = type
                                // Log.i("lala", event!!.title.toString())
                                //Log.i("lala", "Event har blitt hentet")

                                arr.add(event!!)

                            }

                            onCallBack(arr)
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {
                        Log.i("lala", "NOE GIKK FEIL MED DATABASEKOBLING!")
                    }

                })

    }

    override fun onCallBack(liste: ArrayList<Event>) {

        dataset.addAll(liste);
    }

    override fun onCallBack2(liste: ArrayList<Event>) {
        dataset2.addAll(liste);
    }

    override fun onCallBack3(liste: ArrayList<Event>) {
        dataset3.addAll(liste)
    }


}