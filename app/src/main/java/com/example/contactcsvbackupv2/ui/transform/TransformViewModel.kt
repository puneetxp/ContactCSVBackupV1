package com.example.contactcsvbackupv2.ui.transform

import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.contactcsvbackupv2.data.Contact

class TransformViewModel : ViewModel() {

    private val _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> = _contacts

    fun getContacts(contentResolver: ContentResolver) {
        val contactList = mutableListOf<Contact>()

        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use { contactCursor ->
            val idIndex = contactCursor.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)

            if (idIndex != -1 && nameIndex != -1) {
                while (contactCursor.moveToNext()) {
                    val id = contactCursor.getString(idIndex)
                    val name = contactCursor.getString(nameIndex)

                    if (id != null) {
                        val phoneNumbers = mutableListOf<String>()
                        val phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )

                        phoneCursor?.use { phoneC ->
                            val numberIndex = phoneC.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (numberIndex != -1) {
                                while (phoneC.moveToNext()) {
                                    phoneC.getString(numberIndex)?.let { phoneNumber ->
                                        phoneNumbers.add(phoneNumber)
                                    }
                                }
                            }
                        }
                        contactList.add(Contact(id, name ?: "", phoneNumbers))
                    }
                }
            }
        }
        _contacts.value = contactList
    }
}
