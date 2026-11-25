import { useForm } from 'react-hook-form'
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

import {
  GENDERS,
  RELIGIONS,
  USER_TYPES,
  type CreateUserRequest,
  type Gender,
  type Religion,
  type User,
  type UserType,
} from './types'
import { useRegisterUser, useUpdateUser, useUser } from './userHooks'

interface UserFormValues {
  firstName: string
  lastName: string
  email: string
  username: string
  phoneNumber: string
  presentAddress: string
  permanentAddress: string
  gender: Gender | ''
  religion: Religion | ''
  userType: UserType | ''
  password: string
}

const EMPTY_FORM: UserFormValues = {
  firstName: '',
  lastName: '',
  email: '',
  username: '',
  phoneNumber: '',
  presentAddress: '',
  permanentAddress: '',
  gender: '',
  religion: '',
  userType: '',
  password: '',
}

function toFormValues(user: User): UserFormValues {
  return {
    firstName: user.firstName,
    lastName: user.lastName ?? '',
    email: user.email,
    username: user.username ?? user.email,
    phoneNumber: user.phoneNumber,
    presentAddress: user.presentAddress,
    permanentAddress: user.permanentAddress ?? '',
    gender: user.gender,
    religion: user.religion ?? '',
    userType: user.userType,
    password: '',
  }
}

function toProfile(values: UserFormValues) {
  return {
    firstName: values.firstName.trim(),
    lastName: blankToUndefined(values.lastName),
    phoneNumber: values.phoneNumber.trim(),
    presentAddress: values.presentAddress.trim(),
    permanentAddress: blankToUndefined(values.permanentAddress),
    gender: values.gender as Gender,
    religion: values.religion || undefined,
    userType: values.userType as UserType,
  }
}

export function UserFormPage() {
  const params = useParams()
  const userId = params.id ? Number(params.id) : undefined
  const isEdit = userId !== undefined
  const navigate = useNavigate()
  const toast = useToast()

  const user = useUser(userId)
  const register = useRegisterUser()
  const update = useUpdateUser(userId ?? 0)

  const {
    register: field,
    handleSubmit,
    setError,
    setValue,
    getValues,
    formState: { errors },
  } = useForm<UserFormValues>({ values: user.data ? toFormValues(user.data) : EMPTY_FORM })

  const saving = isEdit ? update.isPending : register.isPending

  function onError(error: unknown) {
    if (!applyServerErrors(error, setError)) {
      toast.error(getErrorMessage(error))
    }
  }

  function onSubmit(values: UserFormValues) {
    if (isEdit) {
      update.mutate(toProfile(values), {
        onSuccess: () => {
          toast.success('User updated')
          navigate('/users')
        },
        onError,
      })
      return
    }
    const request: CreateUserRequest = {
      ...toProfile(values),
      email: values.email.trim(),
      username: blankToUndefined(values.username),
      password: values.password,
    }
    register.mutate(request, {
      onSuccess: (created) => {
        toast.success(`${created.firstName} is registered. They must change the temporary password at the first login.`)
        navigate('/users')
      },
      onError,
    })
  }

  if (isEdit && user.isPending) {
    return <Spinner />
  }
  if (isEdit && user.isError) {
    return <ErrorState error={user.error} onRetry={() => void user.refetch()} />
  }

  return (
    <>
      <PageHeader
        title={isEdit ? 'Edit user' : 'Register user'}
        subtitle={isEdit ? undefined : 'Fields marked * are required'}
      />
      <form className="card form" onSubmit={handleSubmit(onSubmit)} noValidate>
        <div className="form-grid">
          <FormField label="First name" htmlFor="firstName" required error={errors.firstName?.message}>
            <input id="firstName" maxLength={100} {...field('firstName', { required: 'First name is required' })} />
          </FormField>
          <FormField label="Last name" htmlFor="lastName" error={errors.lastName?.message}>
            <input id="lastName" maxLength={100} {...field('lastName')} />
          </FormField>
          <FormField
            label="Email"
            htmlFor="email"
            required
            hint={isEdit ? 'The email cannot be changed.' : undefined}
            error={errors.email?.message}
          >
            <input
              id="email"
              type="email"
              readOnly={isEdit}
              {...field('email', {
                required: 'Email is required',
                pattern: { value: /^\S+@\S+\.\S+$/, message: 'Email must be a valid email address' },
              })}
            />
          </FormField>
          <FormField
            label="Username"
            htmlFor="username"
            hint={
              isEdit
                ? 'The username is the login name and cannot be changed.'
                : 'The login name. Leave empty to log in with the email.'
            }
            error={errors.username?.message}
          >
            <input
              id="username"
              maxLength={100}
              autoComplete="off"
              readOnly={isEdit}
              {...field('username', {
                pattern: {
                  value: /^[A-Za-z0-9._@-]{3,100}$/,
                  message: 'Use 3 to 100 letters, numbers, dot, underscore, hyphen or @',
                },
              })}
            />
          </FormField>
          <FormField label="Phone number" htmlFor="phoneNumber" required error={errors.phoneNumber?.message}>
            <input
              id="phoneNumber"
              {...field('phoneNumber', {
                required: 'Phone number is required',
                pattern: { value: /^\+?[0-9\- ]{7,20}$/, message: 'Phone number is not valid' },
              })}
            />
          </FormField>
          <FormField label="Gender" htmlFor="gender" required error={errors.gender?.message}>
            <select id="gender" {...field('gender', { required: 'Gender is required' })}>
              <option value="">Select…</option>
              {GENDERS.map((gender) => (
                <option key={gender} value={gender}>
                  {humanize(gender)}
                </option>
              ))}
            </select>
          </FormField>
          <FormField label="Religion" htmlFor="religion" error={errors.religion?.message}>
            <select id="religion" {...field('religion')}>
              <option value="">Select…</option>
              {RELIGIONS.map((religion) => (
                <option key={religion} value={religion}>
                  {humanize(religion)}
                </option>
              ))}
            </select>
          </FormField>
        </div>

        <FormField label="Present address" htmlFor="presentAddress" required error={errors.presentAddress?.message}>
          <textarea
            id="presentAddress"
            rows={2}
            {...field('presentAddress', {
              required: 'Present address is required',
              maxLength: { value: 500, message: 'Present address must be at most 500 characters' },
            })}
          />
        </FormField>
        <FormField label="Permanent address" htmlFor="permanentAddress" error={errors.permanentAddress?.message}>
          <textarea
            id="permanentAddress"
            rows={2}
            {...field('permanentAddress', {
              maxLength: { value: 500, message: 'Permanent address must be at most 500 characters' },
            })}
          />
        </FormField>
        <div>
          <Button variant="ghost" onClick={() => setValue('permanentAddress', getValues('presentAddress'))}>
            Same as present address
          </Button>
        </div>

        <div className="form-grid">
          <FormField
            label="User type"
            htmlFor="userType"
            required
            hint="Decides what the user is allowed to do."
            error={errors.userType?.message}
          >
            <select id="userType" {...field('userType', { required: 'User type is required' })}>
              <option value="">Select…</option>
              {USER_TYPES.map((type) => (
                <option key={type} value={type}>
                  {humanize(type)}
                </option>
              ))}
            </select>
          </FormField>
          {!isEdit && (
            <FormField
              label="Temporary password"
              htmlFor="password"
              required
              hint="At least 8 characters. The user must change it at the first login."
              error={errors.password?.message}
            >
              <input
                id="password"
                type="password"
                autoComplete="new-password"
                {...field('password', {
                  required: 'Temporary password is required',
                  minLength: { value: 8, message: 'Password must be between 8 and 64 characters' },
                  maxLength: { value: 64, message: 'Password must be between 8 and 64 characters' },
                })}
              />
            </FormField>
          )}
        </div>

        <div className="row row-end">
          <Button onClick={() => navigate('/users')}>Cancel</Button>
          <Button type="submit" variant="primary" loading={saving}>
            {isEdit ? 'Save changes' : 'Register user'}
          </Button>
        </div>
      </form>
    </>
  )
}
