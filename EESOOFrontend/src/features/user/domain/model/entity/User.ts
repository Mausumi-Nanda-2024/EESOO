import { Email } from "../valueobject/Email";
import { FirstName } from "../valueobject/Firstname";
import { LastName } from "../valueobject/Lastname";
import { Pin } from "../valueobject/Pin";
import { PhoneNumber } from "../valueobject/PhoneNumber";


export class User {

    private constructor(
        private readonly firstName: FirstName,
        private readonly lastName: LastName,
        private readonly phoneNumber: PhoneNumber,
        private readonly pin: Pin,
        private readonly email?: Email,
    ) { }

    static register(
        firstName: FirstName,
        lastName: LastName,
        phoneNumber: PhoneNumber,
        pin: Pin,
        email?: Email
    ): User {
        return new User(firstName, lastName, phoneNumber, pin, email);
    }

    getFirstName(): FirstName {
        return this.firstName;
    }

    getLastName(): LastName {
        return this.lastName;
    }

    getPhoneNumber(): PhoneNumber {
        return this.phoneNumber;
    }

    getPin(): Pin {
        return this.pin;
    }

    getEmail(): Email | undefined {
        return this.email;
    }

}
