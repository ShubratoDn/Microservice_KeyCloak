import { useState } from 'react'
import { useForm } from 'react-hook-form'

import { getErrorMessage } from '@/api/ApiError'
import { Permissions } from '@/auth/permissions'
import { useCurrentUser } from '@/auth/useCurrentUser'
import { Button } from '@/components/Button'
import { ConfirmDialog } from '@/components/ConfirmDialog'
import { EmptyState } from '@/components/EmptyState'
import { ErrorState } from '@/components/ErrorState'
import { FormField } from '@/components/FormField'
import { PageHeader } from '@/components/PageHeader'
import { Spinner } from '@/components/Spinner'
import { useToast } from '@/components/toast/toastContext'
import { applyServerErrors, blankToUndefined } from '@/lib/forms'

import { useCategories, useDeleteCategory, useSaveCategory } from './categoryHooks'
import type { Category } from './categoryTypes'

interface CategoryFormValues {
  name: string
  description: string
}

const EMPTY: CategoryFormValues = { name: '', description: '' }

function CategoryForm({ category, onDone }: { category: Category | null; onDone: () => void }) {
  const toast = useToast()
  const save = useSaveCategory(category?.id)
  const {
    register,
    handleSubmit,
    setError,
    reset,
    formState: { errors },
  } = useForm<CategoryFormValues>({
    defaultValues: category ? { name: category.name, description: category.description ?? '' } : EMPTY,
  })

  function onSubmit(values: CategoryFormValues) {
    save.mutate(
      { name: values.name.trim(), description: blankToUndefined(values.description) },
      {
        onSuccess: () => {
          toast.success(category ? 'Category updated' : 'Category created')
          reset(EMPTY)
          onDone()
        },
        onError: (error) => {
          if (!applyServerErrors(error, setError)) {
            toast.error(getErrorMessage(error))
          }
        },
      },
    )
  }

  return (
    <form className="card form" onSubmit={handleSubmit(onSubmit)} noValidate>
      <h2>{category ? `Edit “${category.name}”` : 'New category'}</h2>
      <div className="form-grid">
        <FormField label="Name" htmlFor="category-name" error={errors.name?.message}>
          <input id="category-name" maxLength={100} {...register('name', { required: 'Category name is required' })} />
        </FormField>
        <FormField label="Description" htmlFor="category-description" error={errors.description?.message}>
          <input
            id="category-description"
            {...register('description', { maxLength: { value: 500, message: 'Description must be at most 500 characters' } })}
          />
        </FormField>
      </div>
      <div className="row row-end">
        {category && <Button onClick={onDone}>Cancel</Button>}
        <Button type="submit" variant="primary" loading={save.isPending}>
          {category ? 'Save changes' : 'Create category'}
        </Button>
      </div>
    </form>
  )
}

export function CategoriesPage() {
  const { can } = useCurrentUser()
  const toast = useToast()
  const categories = useCategories()
  const deleteCategory = useDeleteCategory()
  const [editing, setEditing] = useState<Category | null>(null)
  const [toDelete, setToDelete] = useState<Category | null>(null)

  function confirmDelete() {
    if (!toDelete) {
      return
    }
    deleteCategory.mutate(toDelete.id, {
      onSuccess: () => toast.success('Category deleted'),
      onError: (error) => toast.error(getErrorMessage(error)),
      onSettled: () => setToDelete(null),
    })
  }

  const canWrite = can(editing ? Permissions.PRODUCT_EDIT : Permissions.PRODUCT_ADD)

  return (
    <>
      <PageHeader title="Categories" subtitle="Group your products" />

      {canWrite && <CategoryForm key={editing?.id ?? 'new'} category={editing} onDone={() => setEditing(null)} />}

      {categories.isPending && <Spinner />}
      {categories.isError && <ErrorState error={categories.error} onRetry={() => void categories.refetch()} />}

      {categories.data && (
        <div className="card">
          {categories.data.length === 0 ? (
            <EmptyState message="No categories yet." />
          ) : (
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Description</th>
                    <th aria-label="Actions" />
                  </tr>
                </thead>
                <tbody>
                  {categories.data.map((category) => (
                    <tr key={category.id}>
                      <td>{category.name}</td>
                      <td>{category.description ?? '—'}</td>
                      <td className="row row-end">
                        {can(Permissions.PRODUCT_EDIT) && <Button onClick={() => setEditing(category)}>Edit</Button>}
                        {can(Permissions.PRODUCT_DELETE) && (
                          <Button variant="danger" onClick={() => setToDelete(category)}>
                            Delete
                          </Button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      <ConfirmDialog
        open={toDelete !== null}
        danger
        title="Delete category"
        message={`Delete “${toDelete?.name ?? ''}”? A category that still has products cannot be deleted.`}
        confirmLabel="Delete"
        loading={deleteCategory.isPending}
        onConfirm={confirmDelete}
        onCancel={() => setToDelete(null)}
      />
    </>
  )
}
