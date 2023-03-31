package com.example.taskapp
import android.os.Parcel
import android.os.Parcelable

data class Child(
    var childId: String? = null,
    var userId: String? = null,
    var email: String? = null,
    var currentPoints: Int = 0,
    val name: String? = null,
    var loanRate: Int = 1,
    var depositRate: Int = 1,
    var maxDepositPercentage: Int = 1,
    var maxLoanPercentage: Int = 1
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(childId)
        parcel.writeString(userId)
        parcel.writeString(email)
        parcel.writeInt(currentPoints)
        parcel.writeString(name)
        parcel.writeInt(loanRate)
        parcel.writeInt(depositRate)
        parcel.writeInt(maxDepositPercentage)
        parcel.writeInt(maxLoanPercentage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Child> {
        override fun createFromParcel(parcel: Parcel): Child {
            return Child(parcel)
        }

        override fun newArray(size: Int): Array<Child?> {
            return arrayOfNulls(size)
        }
    }
}