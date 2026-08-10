import { createFileRoute } from '@tanstack/react-router'
import { BookOpen, Pencil, Plus, Trash2 } from 'lucide-react'
import * as React from 'react'
import {
  useCreateCategory,
  useCreateService,
  useDeleteCategory,
  useDeleteService,
  useGetCategories,
  useGetServices,
  useUpdateCategory,
  useUpdateService,
} from '../../api/generated/endpoints'
import type { CategoryDto, ServiceDto } from '../../api/generated/models'
import Footer from '../../components/Footer'
import Header from '../../components/Header'

export const Route = createFileRoute('/manager/services')({
  component: ManagerServicesContent,
})

function ManagerServicesContent() {
  const [name, setName] = React.useState('')
  const [description, setDescription] = React.useState('')
  const [basePrice, setBasePrice] = React.useState('')
  const [duration, setDuration] = React.useState('60')
  const [categoryId, setCategoryId] = React.useState('')
  const [catName, setCatName] = React.useState('')
  const [catDescription, setCatDescription] = React.useState('')
  const [editingService, setEditingService] = React.useState<ServiceDto | null>(
    null,
  )
  const [editingCategory, setEditingCategory] =
    React.useState<CategoryDto | null>(null)
  const [message, setMessage] = React.useState<string | null>(null)

  const { data: servicesData = [], refetch } = useGetServices()
  const services = (servicesData || []) as ServiceDto[]

  const { data: categoriesData = [], refetch: refetchCategories } =
    useGetCategories()
  const categories = (categoriesData || []) as CategoryDto[]

  const createServiceMutation = useCreateService({
    mutation: {
      onSuccess: () => {
        setMessage('Service package created successfully!')
        resetServiceForm()
        refetch()
      },
      onError: (err: Error) => {
        setMessage(err.message || 'Failed to create service package.')
      },
    },
  })
  const updateServiceMutation = useUpdateService<Error>()

  const deleteServiceMutation = useDeleteService({
    mutation: {
      onSuccess: () => {
        setMessage('Service package deactivated successfully!')
        refetch()
      },
      onError: (err: Error) => {
        setMessage(err.message || 'Failed to deactivate service package.')
      },
    },
  })

  const deleteCategoryMutation = useDeleteCategory({
    mutation: {
      onSuccess: () => {
        setMessage('Category deleted successfully!')
        refetchCategories()
      },
      onError: (err: Error) => {
        setMessage(err.message || 'Failed to delete category.')
      },
    },
  })

  const createCategoryMutation = useCreateCategory({
    mutation: {
      onSuccess: () => {
        setMessage('Category created successfully!')
        resetCategoryForm()
        refetchCategories()
      },
      onError: (err: Error) => {
        setMessage(err.message || 'Failed to create category.')
      },
    },
  })
  const updateCategoryMutation = useUpdateCategory<Error>()

  const resetServiceForm = () => {
    setName('')
    setDescription('')
    setBasePrice('')
    setDuration('60')
    setCategoryId('')
    setEditingService(null)
  }

  const resetCategoryForm = () => {
    setCatName('')
    setCatDescription('')
    setEditingCategory(null)
  }

  const handleCreateService = (e: React.FormEvent) => {
    e.preventDefault()
    setMessage(null)
    const data = {
      name,
      description,
      basePrice: Number(basePrice),
      durationMinutes: Number(duration),
      categoryId,
    }
    if (editingService?.id) {
      updateServiceMutation.mutate(
        { id: editingService.id, data: { ...editingService, ...data } },
        {
          onSuccess: () => {
            setMessage('Service package updated successfully!')
            resetServiceForm()
            refetch()
          },
          onError: (err) =>
            setMessage(err.message || 'Failed to update service package.'),
        },
      )
      return
    }
    createServiceMutation.mutate({
      data,
    })
  }

  const handleCreateCategory = (e: React.FormEvent) => {
    e.preventDefault()
    if (!catName) return
    setMessage(null)
    const data = {
      name: catName,
      description: catDescription || 'Workshop service category',
    }
    if (editingCategory?.id) {
      updateCategoryMutation.mutate(
        { id: editingCategory.id, data: { ...editingCategory, ...data } },
        {
          onSuccess: () => {
            setMessage('Category updated successfully!')
            resetCategoryForm()
            refetchCategories()
          },
          onError: (err) =>
            setMessage(err.message || 'Failed to update category.'),
        },
      )
      return
    }
    createCategoryMutation.mutate({
      data,
    })
  }

  const editService = (service: ServiceDto) => {
    setEditingService(service)
    setName(service.name || '')
    setDescription(service.description || '')
    setBasePrice(String(service.basePrice || 0))
    setDuration(String(service.durationMinutes || 60))
    setCategoryId(service.categoryId || '')
    setMessage(null)
  }

  const editCategory = (category: CategoryDto) => {
    setEditingCategory(category)
    setCatName(category.name || '')
    setCatDescription(category.description || '')
    setMessage(null)
  }

  const handleDeleteService = (id: string) => {
    if (confirm(`Are you sure you want to deactivate service package ${id}?`)) {
      setMessage(null)
      deleteServiceMutation.mutate({ id })
    }
  }

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 sm:p-8 space-y-2">
          <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
            <BookOpen className="w-4 h-4" />
            Service Management
          </div>
          <h1 className="font-heading text-2xl font-bold">
            Catalog Package & Category Administration
          </h1>
          <p className="text-xs text-muted-foreground">
            Create, update, and manage service packages, pricing tiers, and
            maintenance categories.
          </p>
        </section>

        {message && (
          <div
            className={`p-3 rounded-lg text-xs font-semibold ${
              message.includes('successfully')
                ? 'bg-status-completed/10 text-status-completed border border-status-completed/30'
                : 'bg-destructive/10 text-destructive border border-destructive/30'
            }`}
          >
            {message}
          </div>
        )}

        {/* Forms Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Create Service Package Form */}
          <section className="lg:col-span-2 bg-card border border-border rounded-xl p-6 space-y-4">
            <h2 className="font-heading text-lg font-bold flex items-center gap-2">
              {editingService ? (
                <Pencil className="w-4 h-4 text-primary" />
              ) : (
                <Plus className="w-4 h-4 text-primary" />
              )}
              {editingService
                ? 'Edit Service Package'
                : 'Create New Service Package'}
            </h2>

            <form
              onSubmit={handleCreateService}
              className="grid grid-cols-1 sm:grid-cols-2 gap-4"
            >
              <div>
                <label
                  htmlFor="sName"
                  className="block text-xs font-semibold text-muted-foreground mb-1"
                >
                  Package Name
                </label>
                <input
                  id="sName"
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. Major Synthetic Oil Service"
                  className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
                />
              </div>

              <div>
                <label
                  htmlFor="sPrice"
                  className="block text-xs font-semibold text-muted-foreground mb-1"
                >
                  Base Price (RM)
                </label>
                <input
                  id="sPrice"
                  type="number"
                  step="0.01"
                  required
                  value={basePrice}
                  onChange={(e) => setBasePrice(e.target.value)}
                  placeholder="e.g. 180.00"
                  className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
                />
              </div>

              <div>
                <label
                  htmlFor="sDuration"
                  className="block text-xs font-semibold text-muted-foreground mb-1"
                >
                  Estimated Labor Duration (Minutes)
                </label>
                <input
                  id="sDuration"
                  type="number"
                  required
                  value={duration}
                  onChange={(e) => setDuration(e.target.value)}
                  className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
                />
              </div>

              <div>
                <label
                  htmlFor="sCategory"
                  className="block text-xs font-semibold text-muted-foreground mb-1"
                >
                  Category
                </label>
                <select
                  id="sCategory"
                  required
                  value={categoryId}
                  onChange={(e) => setCategoryId(e.target.value)}
                  className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
                >
                  <option value="">Choose category</option>
                  {categories.map((category) => (
                    <option key={category.id} value={category.id}>
                      {category.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="sm:col-span-2">
                <label
                  htmlFor="sDesc"
                  className="block text-xs font-semibold text-muted-foreground mb-1"
                >
                  Service Description
                </label>
                <textarea
                  id="sDesc"
                  rows={2}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Service package details..."
                  className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
                />
              </div>

              <div className="sm:col-span-2 flex justify-end">
                <button
                  type="submit"
                  disabled={
                    createServiceMutation.isPending ||
                    updateServiceMutation.isPending
                  }
                  className="py-2.5 px-6 rounded-lg bg-primary text-primary-foreground text-xs font-semibold hover:opacity-90 transition-opacity disabled:opacity-50"
                >
                  {createServiceMutation.isPending ||
                  updateServiceMutation.isPending
                    ? 'Saving...'
                    : editingService
                      ? 'Save Service Package'
                      : 'Create Service Package'}
                </button>
                {editingService && (
                  <button
                    type="button"
                    onClick={resetServiceForm}
                    className="ml-2 rounded-lg border border-border px-4 py-2.5 text-xs font-semibold hover:bg-muted"
                  >
                    Cancel edit
                  </button>
                )}
              </div>
            </form>
          </section>

          {/* Quick Category Form */}
          <section className="bg-card border border-border rounded-xl p-6 space-y-4">
            <h2 className="font-heading text-lg font-bold flex items-center gap-2">
              {editingCategory ? (
                <Pencil className="w-4 h-4 text-primary" />
              ) : (
                <Plus className="w-4 h-4 text-primary" />
              )}
              {editingCategory ? 'Edit Category' : 'New Category'}
            </h2>

            <form onSubmit={handleCreateCategory} className="space-y-4">
              <div>
                <label
                  htmlFor="cName"
                  className="block text-xs font-semibold text-muted-foreground mb-1"
                >
                  Category Name
                </label>
                <input
                  id="cName"
                  type="text"
                  required
                  value={catName}
                  onChange={(e) => setCatName(e.target.value)}
                  placeholder="e.g. Brakes & Suspension"
                  className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
                />
              </div>

              <div>
                <label
                  htmlFor="cDescription"
                  className="block text-xs font-semibold text-muted-foreground mb-1"
                >
                  Description
                </label>
                <input
                  id="cDescription"
                  type="text"
                  value={catDescription}
                  onChange={(e) => setCatDescription(e.target.value)}
                  placeholder="Optional category description"
                  className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
                />
              </div>

              <button
                type="submit"
                disabled={
                  createCategoryMutation.isPending ||
                  updateCategoryMutation.isPending
                }
                className="w-full py-2.5 px-4 rounded-lg bg-primary text-primary-foreground text-xs font-semibold hover:opacity-90 transition-opacity disabled:opacity-50"
              >
                {createCategoryMutation.isPending ||
                updateCategoryMutation.isPending
                  ? 'Saving...'
                  : editingCategory
                    ? 'Save Category'
                    : 'Add Category'}
              </button>
              {editingCategory && (
                <button
                  type="button"
                  onClick={resetCategoryForm}
                  className="w-full rounded-lg border border-border px-4 py-2.5 text-xs font-semibold hover:bg-muted"
                >
                  Cancel edit
                </button>
              )}
            </form>

            {/* Existing Categories List */}
            <div className="pt-4 border-t border-border space-y-2">
              <div className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                Existing Categories ({categories.length})
              </div>
              <div className="space-y-1.5 max-h-48 overflow-y-auto">
                {categories.map((c) => (
                  <div
                    key={c.id}
                    className="flex items-center justify-between p-2 rounded-lg bg-muted/50 border border-border text-xs"
                  >
                    <span className="font-semibold text-foreground">
                      {c.name}
                    </span>
                    <div className="flex items-center gap-1">
                      <button
                        type="button"
                        onClick={() => editCategory(c)}
                        className="text-primary hover:text-primary/70 transition-colors"
                        title="Edit Category"
                      >
                        <Pencil className="w-3.5 h-3.5" />
                      </button>
                      <button
                        type="button"
                        onClick={() => {
                          if (c.id && confirm(`Delete category ${c.name}?`)) {
                            deleteCategoryMutation.mutate({ id: c.id })
                          }
                        }}
                        className="text-muted-foreground hover:text-status-cancelled transition-colors"
                        title="Delete Category"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </section>
        </div>

        {/* Master Catalog Table */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          <h2 className="font-heading text-lg font-bold">
            Catalog Packages List
          </h2>

          {services.length === 0 ? (
            <div className="text-center py-8 text-xs text-muted-foreground">
              No service packages found in catalog.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-border text-muted-foreground font-semibold">
                    <th className="py-3 px-3">ID</th>
                    <th className="py-3 px-3">Name</th>
                    <th className="py-3 px-3">Category</th>
                    <th className="py-3 px-3">Base Price</th>
                    <th className="py-3 px-3">Labor Time</th>
                    <th className="py-3 px-3">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {services.map((s) => (
                    <tr
                      key={s.id}
                      className="hover:bg-muted/50 transition-colors"
                    >
                      <td className="py-3.5 px-3 font-mono font-semibold">
                        {s.id}
                      </td>
                      <td className="py-3.5 px-3 font-medium">{s.name}</td>
                      <td className="py-3.5 px-3 text-muted-foreground">
                        {s.categoryId || 'GENERAL'}
                      </td>
                      <td className="py-3.5 px-3 font-bold text-primary">
                        RM {Number(s.basePrice || 0).toFixed(2)}
                      </td>
                      <td className="py-3.5 px-3">
                        {s.durationMinutes || 60} mins
                      </td>
                      <td className="py-3.5 px-3">
                        <div className="flex items-center gap-1">
                          <button
                            type="button"
                            onClick={() => editService(s)}
                            className="rounded-lg p-1.5 text-primary hover:bg-primary/10 transition-colors"
                            title="Edit Service"
                          >
                            <Pencil className="w-4 h-4" />
                          </button>
                          <button
                            type="button"
                            onClick={() => s.id && handleDeleteService(s.id)}
                            className="rounded-lg p-1.5 text-destructive hover:bg-destructive/10 transition-colors"
                            title="Deactivate Service"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>

      <Footer />
    </div>
  )
}
