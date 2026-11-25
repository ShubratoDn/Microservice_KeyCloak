import { useFieldArray, useForm } from 'react-hook-form'
import { useNavigate, useParams } from 'react-router-dom'

import { getErrorMessage } from '@/api/ApiError'
import { Button } from '@/components/Button'
import { ErrorState } from '@/components/ErrorState'
import { FormField } from '@/components/FormField'
import { PageHeader } from '@/components/PageHeader'
import { Spinner } from '@/components/Spinner'
import { useToast } from '@/components/toast/toastContext'
import { applyServerErrors, blankToUndefined } from '@/lib/forms'
import { humanize } from '@/lib/format'

import { useCustomer, useSaveCustomer } from './customerHooks'
import { ADDRESS_TYPES, type AddressType, type Customer, type CustomerRequest } from './types'

interface AddressFormValues {
  type: AddressType
  street: string
  city: string
  state: string
  postalCode: string
  country: string
}

interface CustomerFormValues {
  firstName: string
  lastName: string
  email: string
  phoneNumber: string
  addresses: AddressFormValues[]
}

const EMPTY_ADDRESS: AddressFormValues = { type: 'HOME', street: '', city: '', state: '', postalCode: '', country: '' }
const EMPTY_FORM: CustomerFormValues = { firstName: '', lastName: '', email: '', phoneNumber: '', addresses: [] }

function toFormValues(customer: Customer): CustomerFormValues {
  return {
    firstName: customer.firstName,
    lastName: customer.lastName,
    email: customer.email,
    phoneNumber: customer.phoneNumber ?? '',
    addresses: customer.addresses.map((address) => ({ ...address, state: address.state ?? '' })),
  }
}

function toRequest(values: CustomerFormValues): CustomerRequest {
  return {
    firstName: values.firstName.trim(),
    lastName: values.lastName.trim(),
    email: values.email.trim(),
    phoneNumber: blankToUndefined(values.phoneNumber),
    addresses: values.addresses.map((address) => ({
      type: address.type,
      street: address.street.trim(),
      city: address.city.trim(),
      state: blankToUndefined(address.state),
      postalCode: address.postalCode.trim(),
      country: address.country.trim(),
    })),
  }
}

export function CustomerFormPage() {
  const params = useParams()
  const customerId = params.id ? Number(params.id) : undefined
  const isEdit = customerId !== undefined
  const navigate = useNavigate()
  const toast = useToast()

  const customer = useCustomer(customerId)
  const save = useSaveCustomer(customerId)

  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<CustomerFormValues>({ values: customer.data ? toFormValues(customer.data) : EMPTY_FORM })
  const addresses = useFieldArray({ control, name: 'addresses' })

  function onSubmit(values: CustomerFormValues) {
    save.mutate(toRequest(values), {
      onSuccess: () => {
        toast.success(isEdit ? 'Customer updated' : 'Customer created')
        navigate('/customers')
      },
      onError: (error) => {
        if (!applyServerErrors(error, setError)) {
          toast.error(getErrorMessage(error))
        }
      },
    })
  }

  if (isEdit && customer.isPending) {
    return <Spinner />
  }
  if (isEdit && customer.isError) {
    return <ErrorState error={customer.error} onRetry={() => void customer.refetch()} />
  }

  return (
    <>
      <PageHeader title={isEdit ? 'Edit customer' : 'New customer'} />
      <form className="card form" onSubmit={handleSubmit(onSubmit)} noValidate>
        <div className="form-grid">
          <FormField label="First name" htmlFor="firstName" error={errors.firstName?.message}>
            <input
              id="firstName"
              maxLength={100}
              {...register('firstName', { required: 'First name is required' })}
            />
          </FormField>
          <FormField label="Last name" htmlFor="lastName" error={errors.lastName?.message}>
            <input id="lastName" maxLength={100} {...register('lastName', { required: 'Last name is required' })} />
          </FormField>
          <FormField label="Email" htmlFor="email" error={errors.email?.message}>
            <input
              id="email"
              type="email"
              {...register('email', {
                required: 'Email is required',
                pattern: { value: /^\S+@\S+\.\S+$/, message: 'Email must be a valid email address' },
              })}
            />
          </FormField>
          <FormField
            label="Phone number"
            htmlFor="phoneNumber"
            hint="Optional. Digits, spaces and dashes, 7 to 20 characters."
            error={errors.phoneNumber?.message}
          >
            <input
              id="phoneNumber"
              {...register('phoneNumber', {
                pattern: { value: /^(\+?[0-9\- ]{7,20})?$/, message: 'Phone number is not valid' },
              })}
            />
          </FormField>
        </div>

        <div className="section-title">
          <h2>Addresses</h2>
          <Button onClick={() => addresses.append(EMPTY_ADDRESS)}>Add address</Button>
        </div>

        {addresses.fields.length === 0 && <p className="muted">No addresses added.</p>}

        {addresses.fields.map((field, index) => (
          <fieldset key={field.id} className="fieldset">
            <legend>Address {index + 1}</legend>
            <div className="form-grid">
              <FormField label="Type" htmlFor={`addresses.${index}.type`}>
                <select id={`addresses.${index}.type`} {...register(`addresses.${index}.type`)}>
                  {ADDRESS_TYPES.map((type) => (
                    <option key={type} value={type}>
                      {humanize(type)}
                    </option>
                  ))}
                </select>
              </FormField>
              <FormField label="Street" htmlFor={`addresses.${index}.street`} error={errors.addresses?.[index]?.street?.message}>
                <input
                  id={`addresses.${index}.street`}
                  {...register(`addresses.${index}.street`, { required: 'Street is required' })}
                />
              </FormField>
              <FormField label="City" htmlFor={`addresses.${index}.city`} error={errors.addresses?.[index]?.city?.message}>
                <input
                  id={`addresses.${index}.city`}
                  {...register(`addresses.${index}.city`, { required: 'City is required' })}
                />
              </FormField>
              <FormField label="State" htmlFor={`addresses.${index}.state`} error={errors.addresses?.[index]?.state?.message}>
                <input id={`addresses.${index}.state`} {...register(`addresses.${index}.state`)} />
              </FormField>
              <FormField
                label="Postal code"
                htmlFor={`addresses.${index}.postalCode`}
                error={errors.addresses?.[index]?.postalCode?.message}
              >
                <input
                  id={`addresses.${index}.postalCode`}
                  {...register(`addresses.${index}.postalCode`, { required: 'Postal code is required' })}
                />
              </FormField>
              <FormField
                label="Country"
                htmlFor={`addresses.${index}.country`}
                error={errors.addresses?.[index]?.country?.message}
              >
                <input
                  id={`addresses.${index}.country`}
                  {...register(`addresses.${index}.country`, { required: 'Country is required' })}
                />
              </FormField>
            </div>
            <Button variant="ghost" onClick={() => addresses.remove(index)}>
              Remove address
            </Button>
          </fieldset>
        ))}

        <div className="row row-end">
          <Button onClick={() => navigate('/customers')}>Cancel</Button>
          <Button type="submit" variant="primary" loading={save.isPending}>
            {isEdit ? 'Save changes' : 'Create customer'}
          </Button>
        </div>
      </form>
    </>
  )
}
